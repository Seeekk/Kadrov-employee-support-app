package com.ozhd.kadrov.employeesupport.ui.hr;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrSimpleListBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrEmployeesViewModel;

import java.util.ArrayList;
import java.util.List;

public class HrRegistrationsFragment extends Fragment {

    public static HrRegistrationsFragment newInstance() {
        return new HrRegistrationsFragment();
    }

    private FragmentHrSimpleListBinding binding;
    private HrEmployeesViewModel viewModel;
    private final PendingUsersAdapter adapter = new PendingUsersAdapter();

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
        viewModel = new ViewModelProvider(this).get(HrEmployeesViewModel.class);
        binding.fabAdd.setVisibility(View.GONE);
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            List<UserEntity> pending = new ArrayList<>();
            if (users != null) {
                for (UserEntity u : users) {
                    if (u.approvalStatus == ApprovalStatus.PENDING) {
                        pending.add(u);
                    }
                }
            }
            adapter.setData(pending);
        });
    }

    private void showApprovalDialog(@NonNull UserEntity user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_registration_approval, null, false);
        AutoCompleteTextView role = dialogView.findViewById(R.id.input_role);
        AutoCompleteTextView position = dialogView.findViewById(R.id.input_position);
        AutoCompleteTextView gender = dialogView.findViewById(R.id.input_gender);
        AutoCompleteTextView militaryDoc = dialogView.findViewById(R.id.input_military_doc);

        role.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{getString(R.string.role_employee), getString(R.string.role_hr_long)}));
        role.setText(getString(R.string.role_employee), false);
        gender.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{getString(R.string.gender_male), getString(R.string.gender_female)}));
        gender.setText(getString(R.string.gender_male), false);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.hr_registration_request)
                .setMessage(user.email)
                .setView(dialogView)
                .setPositiveButton(R.string.request_approve, (d, w) -> {
                    String pos = position.getText() != null ? position.getText().toString().trim() : "";
                    if (pos.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.profile_position_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Gender g = getString(R.string.gender_female)
                            .equalsIgnoreCase(String.valueOf(gender.getText()))
                            ? Gender.FEMALE : Gender.MALE;
                    String doc = militaryDoc.getText() != null ? militaryDoc.getText().toString().trim() : "";
                    if (g == Gender.MALE && TextUtils.isEmpty(doc)) {
                        Toast.makeText(requireContext(), R.string.profile_military_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UserRole r = getString(R.string.role_hr_long)
                            .equalsIgnoreCase(String.valueOf(role.getText()))
                            ? UserRole.HR : UserRole.EMPLOYEE;
                    viewModel.getRepository().resolveRegistration(user.id, true, r, pos, g, doc);
                })
                .setNeutralButton(R.string.request_reject, (d, w) ->
                        viewModel.getRepository().resolveRegistration(
                                user.id, false, UserRole.EMPLOYEE, "", Gender.MALE, null))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class PendingUsersAdapter extends RecyclerView.Adapter<VH> {
        private final List<UserEntity> items = new ArrayList<>();

        void setData(List<UserEntity> users) {
            items.clear();
            if (users != null) items.addAll(users);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            UserEntity u = items.get(position);
            holder.binding.textTitle.setText(u.fullName);
            holder.binding.textSubtitle.setText(u.email + " · " + getString(R.string.approval_pending));
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.buttonDelete.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> showApprovalDialog(u));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class VH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        VH(ItemHrTwoLineBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}
