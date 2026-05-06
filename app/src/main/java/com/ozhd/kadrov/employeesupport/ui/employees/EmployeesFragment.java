package com.ozhd.kadrov.employeesupport.ui.employees;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.hr.HrConsoleFragment;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrEmployeesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Раздел «Сотрудники»: подвкладки по категориям списков; переключение темы — как на «Главной».
 */
public class EmployeesFragment extends Fragment {

    public static EmployeesFragment newInstance() {
        return new EmployeesFragment();
    }

    private RecyclerView recyclerView;
    private TextView emptyView;
    private MaterialButtonToggleGroup filterToggle;
    private MaterialButton buttonOpenAdmin;
    private HrEmployeesViewModel viewModel;
    private List<UserEntity> latestUsers;
    private boolean isHr;
    private boolean dismissedMode;
    private final UsersAdapter adapter = new UsersAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employees, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HrEmployeesViewModel.class);
        isHr = RoleHelper.isHr(new SessionManager(requireContext()).getRole());
        recyclerView = view.findViewById(R.id.recycler);
        emptyView = view.findViewById(R.id.text_empty);
        filterToggle = view.findViewById(R.id.toggle_employee_filter);
        buttonOpenAdmin = view.findViewById(R.id.button_open_hr_console);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (!isHr) {
            filterToggle.setVisibility(View.GONE);
            buttonOpenAdmin.setVisibility(View.GONE);
        } else {
            buttonOpenAdmin.setVisibility(View.VISIBLE);
            buttonOpenAdmin.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, HrConsoleFragment.newInstance())
                    .addToBackStack("hr_console")
                    .commit());
            filterToggle.check(R.id.button_filter_active);
            filterToggle.addOnButtonCheckedListener((group, checkedId, checked) -> {
                if (!checked) return;
                dismissedMode = checkedId == R.id.button_filter_dismissed;
                renderList();
            });
        }

        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            latestUsers = users;
            renderList();
        });
    }

    @Override
    public void onDestroyView() {
        recyclerView = null;
        emptyView = null;
        filterToggle = null;
        buttonOpenAdmin = null;
        super.onDestroyView();
    }

    private void renderList() {
        adapter.setData(latestUsers, isHr, dismissedMode);
        if (emptyView != null) {
            emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private class UsersAdapter extends RecyclerView.Adapter<UserVH> {
        private final List<UserEntity> items = new ArrayList<>();

        void setData(List<UserEntity> users, boolean hrView, boolean showDismissed) {
            items.clear();
            if (users != null) {
                for (UserEntity user : users) {
                    if (!hrView) {
                        if (user.isActive && user.approvalStatus == ApprovalStatus.APPROVED) {
                            items.add(user);
                        }
                        continue;
                    }
                    boolean dismissed = !user.isActive && user.dismissalReason != null
                            && !user.dismissalReason.trim().isEmpty();
                    if (showDismissed) {
                        if (dismissed) {
                            items.add(user);
                        }
                    } else if (!dismissed) {
                        items.add(user);
                    }
                }
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
            String pos = u.position != null ? u.position : "—";
            if (isHr) {
                String suffix = (u.dismissalReason != null && !u.dismissalReason.trim().isEmpty())
                        ? getString(R.string.emp_tab_dismissed) + ": " + u.dismissalReason
                        : approvalLabel(u.approvalStatus);
                holder.binding.textSubtitle.setText(u.email + " · " + pos + " · " + suffix);
            } else {
                holder.binding.textSubtitle.setText(u.email + " · " + pos);
            }
            holder.binding.buttonEdit.setVisibility(View.GONE);
            if (isHr) {
                holder.binding.buttonDelete.setVisibility(View.VISIBLE);
                holder.binding.buttonDelete.setOnClickListener(v -> confirmHrUserAction(u));
                holder.binding.getRoot().setOnClickListener(v -> confirmHrUserAction(u));
            } else {
                holder.binding.buttonDelete.setVisibility(View.GONE);
                holder.binding.getRoot().setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String approvalLabel(ApprovalStatus s) {
            if (s == ApprovalStatus.APPROVED) return getString(R.string.approval_approved);
            if (s == ApprovalStatus.REJECTED) return getString(R.string.approval_rejected);
            return getString(R.string.approval_pending);
        }
    }

    private void confirmHrUserAction(@NonNull UserEntity u) {
        String self = new SessionManager(requireContext()).getUserIdOrEmpty();
        if (!self.isEmpty() && self.equals(u.id)) {
            Toast.makeText(requireContext(), R.string.hr_delete_self_forbidden, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(u.fullName)
                .setMessage(R.string.hr_user_actions_hint)
                .setPositiveButton(R.string.hr_user_dismiss, (di, w) -> showDismissDialog(u))
                .setNeutralButton(R.string.hr_user_delete, (di, w) ->
                        viewModel.getRepository().deleteUser(u.id))
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

    private static class UserVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        UserVH(ItemHrTwoLineBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}
