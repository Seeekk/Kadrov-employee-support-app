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
import com.ozhd.kadrov.employeesupport.core.DemoAccounts;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrSimpleListBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrEmployeesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * HR: CRUD учётных записей сотрудников (роль {@link UserRole} задаётся строкой EMPLOYEE / HR).
 */
public class HrEmployeesFragment extends Fragment {

    public static HrEmployeesFragment newInstance() {
        return new HrEmployeesFragment();
    }

    private FragmentHrSimpleListBinding binding;
    private HrEmployeesViewModel viewModel;
    private final UserAdapter adapter = new UserAdapter();

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
        viewModel = new ViewModelProvider(this).get(HrEmployeesViewModel.class);
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        binding.fabAdd.setContentDescription(getString(R.string.hr_add_employee));
        viewModel.getUsers().observe(getViewLifecycleOwner(), adapter::setData);
        binding.fabAdd.setOnClickListener(v -> showUserDialog(null));
    }

    private void showUserDialog(@Nullable UserEntity existing) {
        LinearLayout ll = new LinearLayout(requireContext());
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(p, p, p, p);
        ll.setOrientation(LinearLayout.VERTICAL);
        EditText email = new EditText(requireContext());
        email.setHint(R.string.hr_employee_email);
        EditText fullName = new EditText(requireContext());
        fullName.setHint(R.string.hr_employee_name);
        EditText deptId = new EditText(requireContext());
        deptId.setHint(R.string.hr_employee_dept_id);
        EditText role = new EditText(requireContext());
        role.setHint("EMPLOYEE / HR");
        if (existing != null) {
            email.setText(existing.email);
            fullName.setText(existing.fullName);
            deptId.setText(existing.departmentId != null ? existing.departmentId : "");
            role.setText(existing.role.name());
        } else {
            deptId.setText(DemoAccounts.SEED_DEPARTMENT_ID);
            role.setText(UserRole.EMPLOYEE.name());
        }
        ll.addView(email);
        ll.addView(fullName);
        ll.addView(deptId);
        ll.addView(role);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hr_add_employee)
                .setView(ll)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String em = email.getText() != null ? email.getText().toString().trim() : "";
                    String fn = fullName.getText() != null ? fullName.getText().toString().trim() : "";
                    if (em.isEmpty() || fn.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.login_error_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UserEntity u = new UserEntity();
                    u.id = existing != null ? existing.id : DatabaseSeeder.newId();
                    u.email = em;
                    u.fullName = fn;
                    u.departmentId = deptId.getText() != null ? deptId.getText().toString().trim() : null;
                    u.role = UserRole.fromString(role.getText() != null ? role.getText().toString() : "");
                    u.isActive = true;
                    u.updatedAt = System.currentTimeMillis();
                    u.isSynced = false;
                    viewModel.getRepository().upsertUser(u);
                    Toast.makeText(requireContext(), R.string.hr_sync_stub, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDelete(@NonNull UserEntity u) {
        String self = new SessionManager(requireContext()).getUserIdOrEmpty();
        if (!self.isEmpty() && self.equals(u.id)) {
            Toast.makeText(requireContext(), R.string.hr_delete_self_forbidden, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.hr_delete_confirm)
                .setPositiveButton(android.R.string.ok, (di, w) -> viewModel.getRepository().deleteUser(u.id))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class UserAdapter extends RecyclerView.Adapter<UserVH> {
        private final List<UserEntity> items = new ArrayList<>();

        void setData(List<UserEntity> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull UserVH holder, int position) {
            UserEntity u = items.get(position);
            holder.binding.textTitle.setText(u.fullName);
            holder.binding.textSubtitle.setText(u.email + " · " + u.role.name());
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> showUserDialog(u));
            holder.binding.buttonDelete.setOnClickListener(v -> confirmDelete(u));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class UserVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        UserVH(ItemHrTwoLineBinding binding) {
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
