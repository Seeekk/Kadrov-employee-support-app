package com.ozhd.kadrov.employeesupport.ui.hr;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder;
import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrSimpleListBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrDepartmentsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * HR: CRUD отделов (локально + заготовка синхронизации с сервером в {@link HrAdminRepository}).
 */
public class HrDepartmentsFragment extends Fragment {

    public static HrDepartmentsFragment newInstance() {
        return new HrDepartmentsFragment();
    }

    private FragmentHrSimpleListBinding binding;
    private HrDepartmentsViewModel viewModel;
    private final DeptAdapter adapter = new DeptAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHrSimpleListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!RoleHelper.ensureHrOrToast(this)) {
            return;
        }
        viewModel = new ViewModelProvider(this).get(HrDepartmentsViewModel.class);
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        binding.fabAdd.setContentDescription(getString(R.string.hr_add_department));
        viewModel.getDepartments().observe(getViewLifecycleOwner(), adapter::setData);

        binding.fabAdd.setOnClickListener(v -> showEditDialog(null));
    }

    private void showEditDialog(@Nullable DepartmentEntity existing) {
        LinearLayout ll = new LinearLayout(requireContext());
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(p, p, p, p);
        ll.setOrientation(LinearLayout.VERTICAL);
        EditText name = new EditText(requireContext());
        name.setHint(R.string.hr_department_name);
        EditText desc = new EditText(requireContext());
        desc.setHint(R.string.hr_department_desc);
        if (existing != null) {
            name.setText(existing.name);
            desc.setText(existing.description != null ? existing.description : "");
        }
        ll.addView(name);
        ll.addView(desc);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.hr_add_department : R.string.hr_department_name)
                .setView(ll)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String n = name.getText() != null ? name.getText().toString().trim() : "";
                    if (n.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.login_error_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DepartmentEntity e = new DepartmentEntity();
                    e.id = existing != null ? existing.id : DatabaseSeeder.newId();
                    e.name = n;
                    e.description = desc.getText() != null ? desc.getText().toString().trim() : null;
                    viewModel.getRepository().upsertDepartment(e);
                    Toast.makeText(requireContext(), R.string.hr_sync_stub, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDelete(@NonNull DepartmentEntity d) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.hr_delete_confirm)
                .setPositiveButton(android.R.string.ok, (di, w) -> viewModel.getRepository().deleteDepartment(d.id))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class DeptAdapter extends RecyclerView.Adapter<DeptVH> {

        private final List<DepartmentEntity> items = new ArrayList<>();

        void setData(List<DepartmentEntity> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DeptVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new DeptVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull DeptVH holder, int position) {
            DepartmentEntity d = items.get(position);
            holder.binding.textTitle.setText(d.name);
            holder.binding.textSubtitle.setText(
                    !TextUtils.isEmpty(d.description) ? d.description : d.id);
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> showEditDialog(d));
            holder.binding.buttonDelete.setOnClickListener(v -> confirmDelete(d));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class DeptVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        DeptVH(ItemHrTwoLineBinding binding) {
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
