package com.ozhd.kadrov.employeesupport.ui.hr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.FieldSchemaJson;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;
import com.ozhd.kadrov.employeesupport.data.repository.HrAdminRepository;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrKindsBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrKindsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * HR: типы заявок и связанные поля — источник для динамических вкладок «Создание заявки».
 */
public class HrRequestKindsFragment extends Fragment {

    public static HrRequestKindsFragment newInstance() {
        return new HrRequestKindsFragment();
    }

    private FragmentHrKindsBinding binding;
    private HrKindsViewModel kindsViewModel;
    private HrAdminRepository hrRepo;
    private final KindAdapter kindAdapter = new KindAdapter();
    private final FieldAdapter fieldAdapter = new FieldAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHrKindsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!RoleHelper.ensureHrOrToast(this)) {
            return;
        }
        hrRepo = new HrAdminRepository(requireContext());
        kindsViewModel = new ViewModelProvider(this).get(HrKindsViewModel.class);

        binding.recyclerKinds.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerKinds.setAdapter(kindAdapter);
        binding.recyclerFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFields.setAdapter(fieldAdapter);

        kindsViewModel.getKinds().observe(getViewLifecycleOwner(), kindAdapter::setData);
        kindsViewModel.observeFieldsForSelection().observe(getViewLifecycleOwner(), fieldAdapter::setData);

        binding.buttonAddKind.setOnClickListener(v -> showKindDialog(null));
        binding.buttonAddField.setOnClickListener(v -> {
            if (kindAdapter.getSelectedKindId() == null) {
                Toast.makeText(requireContext(), R.string.hr_select_kind_first, Toast.LENGTH_SHORT).show();
                return;
            }
            showFieldDialog(null);
        });
    }

    private void showKindDialog(@Nullable RequestKindEntity existing) {
        LinearLayout ll = new LinearLayout(requireContext());
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(p, p, p, p);
        ll.setOrientation(LinearLayout.VERTICAL);
        EditText name = new EditText(requireContext());
        name.setHint(R.string.hr_kind_name);
        EditText desc = new EditText(requireContext());
        desc.setHint(R.string.hr_kind_desc);
        if (existing != null) {
            name.setText(existing.name);
            desc.setText(existing.description != null ? existing.description : "");
        }
        ll.addView(name);
        ll.addView(desc);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.hr_add_kind : R.string.hr_kind_name)
                .setView(ll)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String n = text(name);
                    if (n.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.login_error_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RequestKindEntity k = new RequestKindEntity();
                    k.id = existing != null ? existing.id : DatabaseSeeder.newId();
                    k.name = n;
                    k.description = text(desc).isEmpty() ? null : text(desc);
                    k.isActive = true;
                    k.fieldSchemaJson = existing != null && existing.fieldSchemaJson != null
                            ? existing.fieldSchemaJson : FieldSchemaJson.EXAMPLE_VACATION;
                    k.updatedAt = System.currentTimeMillis();
                    k.isSynced = false;
                    if (existing != null) {
                        k.sortOrder = existing.sortOrder;
                        hrRepo.upsertRequestKind(k);
                        Toast.makeText(requireContext(), R.string.hr_sync_stub, Toast.LENGTH_SHORT).show();
                    } else {
                        // Новый тип: порядок вкладок из MAX(sortOrder)+шаг, без правки кода.
                        hrRepo.computeNextKindSortOrder(order -> {
                            k.sortOrder = order;
                            hrRepo.upsertRequestKind(k);
                        });
                        Toast.makeText(requireContext(), R.string.hr_sync_stub, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showFieldDialog(@Nullable RequestFieldDefinitionEntity existing) {
        if (kindAdapter.getSelectedKindId() == null) {
            return;
        }
        LinearLayout ll = new LinearLayout(requireContext());
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(p, p, p, p);
        ll.setOrientation(LinearLayout.VERTICAL);
        EditText key = new EditText(requireContext());
        key.setHint(R.string.hr_field_key);
        EditText label = new EditText(requireContext());
        label.setHint(R.string.hr_field_label);
        Spinner typeSpin = new Spinner(requireContext());
        FieldInputType[] types = FieldInputType.values();
        List<String> labels = new ArrayList<>();
        for (FieldInputType t : types) {
            labels.add(t.name());
        }
        typeSpin.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, labels));
        if (existing != null) {
            key.setText(existing.fieldKey);
            label.setText(existing.label);
            for (int i = 0; i < types.length; i++) {
                if (types[i] == existing.inputType) {
                    typeSpin.setSelection(i);
                    break;
                }
            }
        }
        ll.addView(key);
        ll.addView(label);
        ll.addView(typeSpin);
        CheckBox required = new CheckBox(requireContext());
        required.setText(R.string.field_required_hint);
        required.setChecked(existing != null && existing.required);
        ll.addView(required);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hr_add_field)
                .setView(ll)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String k = text(key);
                    String lab = text(label);
                    if (k.isEmpty() || lab.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.login_error_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FieldInputType it = types[typeSpin.getSelectedItemPosition()];
                    RequestFieldDefinitionEntity f = new RequestFieldDefinitionEntity();
                    f.id = existing != null ? existing.id : DatabaseSeeder.newId();
                    f.kindId = kindAdapter.getSelectedKindId();
                    f.fieldKey = k;
                    f.label = lab;
                    f.inputType = it;
                    f.required = required.isChecked();
                    f.sortOrder = existing != null ? existing.sortOrder : fieldAdapter.getItemCount();
                    f.validationJson = null;
                    hrRepo.upsertField(f);
                    Toast.makeText(requireContext(), R.string.hr_sync_stub, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static String text(EditText e) {
        return e.getText() != null ? e.getText().toString().trim() : "";
    }

    private void confirmDeleteKind(@NonNull RequestKindEntity k) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.hr_delete_confirm)
                .setPositiveButton(android.R.string.ok, (di, w) -> hrRepo.deleteRequestKind(k.id))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDeleteField(@NonNull RequestFieldDefinitionEntity f) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.hr_delete_confirm)
                .setPositiveButton(android.R.string.ok, (di, w) -> hrRepo.deleteField(f.id))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class KindAdapter extends RecyclerView.Adapter<KindVH> {
        private final List<RequestKindEntity> items = new ArrayList<>();
        private String selectedKindId;

        @Nullable
        String getSelectedKindId() {
            return selectedKindId;
        }

        void setData(List<RequestKindEntity> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public KindVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new KindVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull KindVH holder, int position) {
            RequestKindEntity k = items.get(position);
            holder.binding.textTitle.setText(k.name);
            holder.binding.textSubtitle.setText(k.description != null ? k.description : k.id);
            boolean sel = k.id.equals(selectedKindId);
            holder.binding.getRoot().setAlpha(sel ? 1f : 0.85f);
            holder.binding.buttonEdit.setVisibility(View.VISIBLE);
            holder.binding.buttonEdit.setOnClickListener(v -> showKindDialog(k));
            holder.binding.getRoot().setOnClickListener(v -> {
                selectedKindId = k.id;
                kindsViewModel.selectKind(k.id);
                binding.labelFields.setVisibility(View.VISIBLE);
                binding.recyclerFields.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            });
            holder.binding.buttonDelete.setOnClickListener(v -> confirmDeleteKind(k));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class FieldAdapter extends RecyclerView.Adapter<FieldVH> {
        private final List<RequestFieldDefinitionEntity> items = new ArrayList<>();

        void setData(List<RequestFieldDefinitionEntity> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FieldVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new FieldVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull FieldVH holder, int position) {
            RequestFieldDefinitionEntity f = items.get(position);
            holder.binding.textTitle.setText(f.label);
            holder.binding.textSubtitle.setText(f.fieldKey + " · " + f.inputType.name());
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> showFieldDialog(f));
            holder.binding.buttonDelete.setOnClickListener(v -> confirmDeleteField(f));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class KindVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        KindVH(ItemHrTwoLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class FieldVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        FieldVH(ItemHrTwoLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
