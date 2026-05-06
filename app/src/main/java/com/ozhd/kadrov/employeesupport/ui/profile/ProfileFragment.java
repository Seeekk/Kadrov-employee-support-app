package com.ozhd.kadrov.employeesupport.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.repository.UserRepository;
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
    private UserRepository userRepository;
    private Gender selectedGender = Gender.MALE;

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
        userRepository = new UserRepository(requireContext());
        binding.genderToggle.check(R.id.button_gender_male);
        binding.profileMilitaryToggle.check(R.id.profile_button_military_no);
        binding.genderToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            selectedGender = checkedId == R.id.button_gender_female ? Gender.FEMALE : Gender.MALE;
            binding.layoutMilitaryLiableBlock.setVisibility(
                    selectedGender == Gender.MALE ? View.VISIBLE : View.GONE);
            if (selectedGender == Gender.FEMALE) {
                binding.profileMilitaryToggle.check(R.id.profile_button_military_no);
            }
        });

        sessionViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            FragmentProfileBinding b = binding;
            if (b == null) {
                return;
            }
            if (user == null) {
                b.profileName.setText("—");
                b.profileEmail.setText("");
                b.profileRole.setText("");
                b.inputPosition.setText("");
                b.genderToggle.check(R.id.button_gender_male);
                b.profileMilitaryToggle.check(R.id.profile_button_military_no);
                return;
            }
            b.profileName.setText(user.fullName);
            b.profileEmail.setText(user.email);
            UserRole r = user.role;
            b.profileRole.setText(r == UserRole.HR
                    ? getString(R.string.role_hr)
                    : getString(R.string.role_employee));
            b.inputFullName.setText(user.fullName != null ? user.fullName : "");
            b.inputPosition.setText(user.position != null ? user.position : "");
            selectedGender = user.gender != null ? user.gender : Gender.MALE;
            b.genderToggle.check(selectedGender == Gender.FEMALE
                    ? R.id.button_gender_female
                    : R.id.button_gender_male);
            b.layoutMilitaryLiableBlock.setVisibility(selectedGender == Gender.MALE ? View.VISIBLE : View.GONE);
            if (selectedGender == Gender.MALE) {
                b.profileMilitaryToggle.check(user.isMilitaryLiable
                        ? R.id.profile_button_military_yes
                        : R.id.profile_button_military_no);
            } else {
                b.profileMilitaryToggle.check(R.id.profile_button_military_no);
            }
        });

        binding.buttonLogout.setOnClickListener(v -> AuthUiHelper.showLogoutDialog(requireActivity()));
        binding.buttonSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        FragmentProfileBinding b = binding;
        if (b == null) return;
        String fullName = b.inputFullName.getText() != null ? b.inputFullName.getText().toString().trim() : "";
        String pos = b.inputPosition.getText() != null ? b.inputPosition.getText().toString().trim() : "";
        b.layoutMilitaryLiableBlock.setVisibility(selectedGender == Gender.MALE ? View.VISIBLE : View.GONE);
        boolean militaryLiable = selectedGender == Gender.MALE
                && b.profileMilitaryToggle.getCheckedButtonId() == R.id.profile_button_military_yes;
        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), R.string.register_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (pos.isEmpty()) {
            Toast.makeText(requireContext(), R.string.profile_position_required, Toast.LENGTH_SHORT).show();
            return;
        }
        userRepository.updateCurrentProfile(fullName, pos, selectedGender, militaryLiable);
        Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
