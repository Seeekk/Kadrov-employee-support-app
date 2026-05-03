package com.ozhd.kadrov.employeesupport.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.databinding.FragmentProfileBinding;
import com.ozhd.kadrov.employeesupport.ui.auth.AuthUiHelper;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel;

/**
 * «Мой профиль»: данные текущего пользователя из Room через {@link SessionViewModel}.
 * Тема оформления — выпадающий список на «Главной» над вкладками (см. {@link com.ozhd.kadrov.employeesupport.ui.common.ThemeSpinnerHelper}).
 */
public class ProfileFragment extends Fragment {

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionViewModel sessionViewModel = new ViewModelProvider(requireActivity())
                .get(SessionViewModel.class);

        sessionViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            FragmentProfileBinding b = binding;
            if (b == null) {
                return;
            }
            if (user == null) {
                b.profileName.setText("—");
                b.profileEmail.setText("");
                b.profileRole.setText("");
                return;
            }
            b.profileName.setText(user.fullName);
            b.profileEmail.setText(user.email);
            UserRole r = user.role;
            b.profileRole.setText(r == UserRole.HR
                    ? getString(R.string.role_hr)
                    : getString(R.string.role_employee));
        });

        binding.buttonLogout.setOnClickListener(v -> AuthUiHelper.showLogoutDialog(requireActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
