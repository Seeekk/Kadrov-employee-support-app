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
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
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
        role.setHint(getString(R.string.hr_role_hint));
        EditText position = new EditText(requireContext());
        position.setHint(R.string.profile_position);
        EditText gender = new EditText(requireContext());
        gender.setHint(R.string.profile_gender);
        EditText approval = new EditText(requireContext());
        approval.setHint(R.string.hr_approval_hint);
        EditText military = new EditText(requireContext());
        military.setHint("Военнообязанный: 1/0");
        EditText militaryDoc = new EditText(requireContext());
        militaryDoc.setHint(R.string.profile_military_document);
        if (existing != null) {
            email.setText(existing.email);
            fullName.setText(existing.fullName);
            deptId.setText(existing.departmentId != null ? existing.departmentId : "");
            role.setText(roleLabel(existing.role));
            position.setText(existing.position != null ? existing.position : "");
            gender.setText(genderLabel(existing.gender != null ? existing.gender : Gender.MALE));
            approval.setText(approvalLabel(existing.approvalStatus));
            military.setText(existing.isMilitaryLiable ? "1" : "0");
            militaryDoc.setText(existing.militaryDocument != null ? existing.militaryDocument : "");
        } else {
            deptId.setText(DemoAccounts.SEED_DEPARTMENT_ID);
            role.setText(getString(R.string.role_employee));
            position.setText("");
            gender.setText(getString(R.string.gender_male));
            approval.setText(getString(R.string.approval_approved));
            military.setText("0");
            militaryDoc.setText("");
        }
        ll.addView(email);
        ll.addView(fullName);
        ll.addView(deptId);
        ll.addView(role);
        ll.addView(position);
        ll.addView(gender);
        ll.addView(approval);
        ll.addView(military);
        ll.addView(militaryDoc);

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
                    u.role = parseRole(role.getText() != null ? role.getText().toString() : "");
                    u.position = position.getText() != null ? position.getText().toString().trim() : null;
                    u.gender = parseGender(gender.getText() != null ? gender.getText().toString() : "");
                    u.approvalStatus = parseApprovalStatus(
                            approval.getText() != null ? approval.getText().toString() : "");
                    u.isMilitaryLiable = TextUtils.equals("1", military.getText()) || u.gender == Gender.MALE;
                    u.militaryDocument = militaryDoc.getText() != null
                            ? militaryDoc.getText().toString().trim() : null;
                    u.isActive = u.approvalStatus == ApprovalStatus.APPROVED;
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
                .setTitle(u.fullName)
                .setMessage(R.string.hr_user_actions_hint)
                .setPositiveButton(R.string.hr_user_dismiss, (di, w) -> showDismissDialog(u))
                .setNeutralButton(R.string.hr_user_delete, (di, w) -> viewModel.getRepository().deleteUser(u.id))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDismissDialog(@NonNull UserEntity user) {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.hr_dismiss_reason_hint);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hr_user_dismiss)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String reason = input.getText() != null ? input.getText().toString().trim() : "";
                    if (reason.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.hr_dismiss_reason_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.getRepository().dismissUser(user.id, reason);
                })
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
            String military = u.isMilitaryLiable ? "военнообязанный" : "невоеннообязанный";
            String pos = u.position != null ? u.position : "—";
            holder.binding.textSubtitle.setText(u.email + " · " + roleLabel(u.role)
                    + " · " + pos + " · " + approvalLabel(u.approvalStatus) + " · " + military);
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> showUserDialog(u));
            holder.binding.buttonDelete.setOnClickListener(v -> confirmDelete(u));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private UserRole parseRole(@NonNull String value) {
        String v = value.trim();
        if (v.equalsIgnoreCase(getString(R.string.role_hr_long))
                || v.equalsIgnoreCase(getString(R.string.role_hr))
                || v.equalsIgnoreCase("HR")) {
            return UserRole.HR;
        }
        return UserRole.EMPLOYEE;
    }

    private Gender parseGender(@NonNull String value) {
        String v = value.trim();
        if (v.equalsIgnoreCase(getString(R.string.gender_female))
                || v.equalsIgnoreCase("FEMALE")) {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }

    private ApprovalStatus parseApprovalStatus(@NonNull String value) {
        String v = value.trim();
        if (v.equalsIgnoreCase(getString(R.string.approval_approved)) || v.equalsIgnoreCase("APPROVED")) {
            return ApprovalStatus.APPROVED;
        }
        if (v.equalsIgnoreCase(getString(R.string.approval_rejected)) || v.equalsIgnoreCase("REJECTED")) {
            return ApprovalStatus.REJECTED;
        }
        return ApprovalStatus.PENDING;
    }

    private String roleLabel(UserRole role) {
        return role == UserRole.HR ? getString(R.string.role_hr_long) : getString(R.string.role_employee);
    }

    private String genderLabel(Gender gender) {
        return gender == Gender.FEMALE ? getString(R.string.gender_female) : getString(R.string.gender_male);
    }

    private String approvalLabel(ApprovalStatus s) {
        if (s == ApprovalStatus.APPROVED) return getString(R.string.approval_approved);
        if (s == ApprovalStatus.REJECTED) return getString(R.string.approval_rejected);
        return getString(R.string.approval_pending);
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
