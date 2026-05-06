package com.ozhd.kadrov.employeesupport.ui.notifications;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.NotificationType;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.repository.HrAdminRepository;
import com.ozhd.kadrov.employeesupport.data.repository.NotificationRepository;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrSimpleListBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemHrTwoLineBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Уведомления: фильтры по типам и состоянию прочтения; переключение темы — как на «Главной».
 */
public class NotificationsFragment extends Fragment {

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    private FragmentHrSimpleListBinding binding;
    private NotificationRepository notificationRepository;
    private HrAdminRepository hrAdminRepository;
    private ListenerRegistration realtimeRegistration;
    private boolean registrationOnlyFilter;
    private List<NotificationEntity> allNotifications = new ArrayList<>();
    private final NotificationAdapter adapter = new NotificationAdapter();

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
        notificationRepository = new NotificationRepository(requireContext());
        hrAdminRepository = new HrAdminRepository(requireContext());
        binding.fabAdd.setVisibility(View.VISIBLE);
        binding.fabAdd.setOnClickListener(v -> showFilterDialog());
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        notificationRepository.observeMine().observe(getViewLifecycleOwner(), data -> {
            allNotifications = data != null ? data : new ArrayList<>();
            applyFilter();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (notificationRepository != null) {
            realtimeRegistration = notificationRepository.attachRealtime();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (realtimeRegistration != null) {
            realtimeRegistration.remove();
            realtimeRegistration = null;
        }
    }

    private void onNotificationClick(@NonNull NotificationEntity item) {
        if (item.type == NotificationType.REGISTRATION && item.relatedUserId != null
                && RoleHelper.isHr(new ViewModelProvider(requireActivity())
                .get(SessionViewModel.class).getRoleValue())) {
            showApprovalDialog(item);
            return;
        }
        notificationRepository.markRead(item.id);
    }

    private void showApprovalDialog(@NonNull NotificationEntity notification) {
        LinearLayout ll = new LinearLayout(requireContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        ll.setPadding(p, p, p, p);

        AutoCompleteTextView role = new AutoCompleteTextView(requireContext());
        role.setHint(getString(R.string.hr_role_hint));
        role.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{getString(R.string.role_employee), getString(R.string.role_hr_long)}));
        role.setText(getString(R.string.role_employee), false);

        AutoCompleteTextView position = new AutoCompleteTextView(requireContext());
        position.setHint(getString(R.string.profile_position));
        AutoCompleteTextView gender = new AutoCompleteTextView(requireContext());
        gender.setHint(getString(R.string.profile_gender));
        gender.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{getString(R.string.gender_male), getString(R.string.gender_female)}));
        gender.setText(getString(R.string.gender_male), false);
        AutoCompleteTextView militaryDoc = new AutoCompleteTextView(requireContext());
        militaryDoc.setHint(getString(R.string.profile_military_document));

        ll.addView(role);
        ll.addView(position);
        ll.addView(gender);
        ll.addView(militaryDoc);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(notification.title)
                .setMessage(notification.message)
                .setView(ll)
                .setPositiveButton(R.string.request_approve, (d, w) -> {
                    Gender g = getGenderFromLabel(gender.getText() != null
                            ? gender.getText().toString() : null);
                    String pos = position.getText() != null ? position.getText().toString().trim() : "";
                    String doc = militaryDoc.getText() != null ? militaryDoc.getText().toString().trim() : "";
                    if (pos.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.profile_position_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (g == Gender.MALE && TextUtils.isEmpty(doc)) {
                        Toast.makeText(requireContext(), R.string.profile_military_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    hrAdminRepository.resolveRegistration(
                            notification.relatedUserId,
                            true,
                            getRoleFromLabel(role.getText() != null ? role.getText().toString() : null),
                            pos,
                            g,
                            doc
                    );
                    notificationRepository.markRead(notification.id);
                })
                .setNeutralButton(R.string.request_reject, (d, w) -> {
                    hrAdminRepository.resolveRegistration(
                            notification.relatedUserId,
                            false,
                            UserRole.EMPLOYEE,
                            "",
                            Gender.MALE,
                            null
                    );
                    notificationRepository.markRead(notification.id);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void showFilterDialog() {
        String[] options = new String[]{"Все уведомления", "Только регистрации"};
        int checked = registrationOnlyFilter ? 1 : 0;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notifications_filter_title)
                .setSingleChoiceItems(options, checked, (d, which) -> {
                    registrationOnlyFilter = which == 1;
                    applyFilter();
                    d.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyFilter() {
        if (!registrationOnlyFilter) {
            adapter.setData(allNotifications);
            return;
        }
        List<NotificationEntity> filtered = new ArrayList<>();
        for (NotificationEntity n : allNotifications) {
            if (n.type == NotificationType.REGISTRATION) {
                filtered.add(n);
            }
        }
        adapter.setData(filtered);
    }

    private UserRole getRoleFromLabel(@Nullable CharSequence label) {
        if (label == null) return UserRole.EMPLOYEE;
        String v = label.toString().trim();
        if (getString(R.string.role_hr_long).equalsIgnoreCase(v)
                || getString(R.string.role_hr).equalsIgnoreCase(v)) {
            return UserRole.HR;
        }
        return UserRole.EMPLOYEE;
    }

    private Gender getGenderFromLabel(@Nullable CharSequence label) {
        if (label == null) return Gender.MALE;
        String v = label.toString().trim();
        if (getString(R.string.gender_female).equalsIgnoreCase(v)
                || getString(R.string.gender_female_icon).equalsIgnoreCase(v)) {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationVH> {
        private final List<NotificationEntity> items = new ArrayList<>();

        void setData(List<NotificationEntity> data) {
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemHrTwoLineBinding b = ItemHrTwoLineBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new NotificationVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationVH holder, int position) {
            NotificationEntity n = items.get(position);
            holder.binding.textTitle.setText(n.title);
            holder.binding.textSubtitle.setText(n.message);
            holder.binding.buttonEdit.setVisibility(View.GONE);
            holder.binding.buttonDelete.setVisibility(View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> onNotificationClick(n));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class NotificationVH extends RecyclerView.ViewHolder {
        final ItemHrTwoLineBinding binding;

        NotificationVH(ItemHrTwoLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
