package com.ozhd.kadrov.employeesupport.ui.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;
import com.ozhd.kadrov.employeesupport.data.repository.RequestCatalogRepository;
import com.ozhd.kadrov.employeesupport.data.repository.RequestSubmissionRepository;
import com.ozhd.kadrov.employeesupport.databinding.FragmentDynamicRequestFormBinding;

import java.util.List;
import java.util.Map;

/**
 * Форма создания заявки для одного динамического типа: поля из Room (и резерв из JSON-схемы типа),
 * отправка в {@link RequestSubmissionRepository}. Данные списка полей можно вынести во ViewModel по тому же
 * принципу, что {@link com.ozhd.kadrov.employeesupport.ui.viewmodel.CreateRequestViewModel}.
 */
public class DynamicRequestFormFragment extends Fragment {

    private static final String ARG_KIND_ID = "kindId";
    private static final String ARG_KIND_NAME = "kindName";

    private FragmentDynamicRequestFormBinding binding;
    private List<RequestFieldDefinitionEntity> fieldDefinitions;

    public static DynamicRequestFormFragment newInstance(@NonNull String kindId, @NonNull String kindName) {
        DynamicRequestFormFragment f = new DynamicRequestFormFragment();
        Bundle b = new Bundle();
        b.putString(ARG_KIND_ID, kindId);
        b.putString(ARG_KIND_NAME, kindName);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDynamicRequestFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;
        String kindId = args.getString(ARG_KIND_ID);
        String kindName = args.getString(ARG_KIND_NAME);
        if (kindId == null) return;

        RequestCatalogRepository catalog = new RequestCatalogRepository(requireContext());
        // Поля из Room или fallback из JSON-схемы типа заявки (без изменения кода приложения).
        catalog.observeResolvedFieldsForKind(kindId).observe(getViewLifecycleOwner(), fields -> {
            fieldDefinitions = fields;
            LinearLayout container = binding.fieldsContainer;
            if (fields == null || fields.isEmpty()) {
                container.removeAllViews();
                return;
            }
            DynamicFormInflater.inflateInto(container, fields);
        });

        binding.buttonSubmit.setOnClickListener(v -> submit(kindId, kindName != null ? kindName : ""));
    }

    private void submit(@NonNull String kindId, @NonNull String kindTitle) {
        if (fieldDefinitions == null || fieldDefinitions.isEmpty()) {
            Toast.makeText(requireContext(), R.string.request_no_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> values = DynamicFormInflater.collectValues(binding.fieldsContainer);
        for (RequestFieldDefinitionEntity f : fieldDefinitions) {
            if (!f.required || f.inputType == FieldInputType.FILE) {
                continue;
            }
            String v = values.get(f.fieldKey);
            if (v == null || v.isEmpty()) {
                Toast.makeText(requireContext(),
                        getString(R.string.request_fill_required, f.label),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
        SessionManager sm = new SessionManager(requireContext());
        String uid = sm.getUserIdOrEmpty();
        if (uid.isEmpty()) {
            Toast.makeText(requireContext(), R.string.request_no_session, Toast.LENGTH_SHORT).show();
            return;
        }
        RequestSubmissionRepository submitter = new RequestSubmissionRepository(requireContext());
        submitter.submitAsync(uid, kindId, kindTitle, "", values);
        Toast.makeText(requireContext(), R.string.request_sent_local, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
