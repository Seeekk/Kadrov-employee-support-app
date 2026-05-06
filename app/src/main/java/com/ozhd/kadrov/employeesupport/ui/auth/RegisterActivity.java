package com.ozhd.kadrov.employeesupport.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.databinding.ActivityRegisterBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.LoginViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getState().observe(this, state -> {
            switch (state.phase) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.buttonRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonRegister.setEnabled(true);
                    Toast.makeText(this, R.string.register_success_pending, Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonRegister.setEnabled(true);
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
                    break;
                case IDLE:
                default:
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonRegister.setEnabled(true);
                    break;
            }
        });

        binding.buttonRegister.setOnClickListener(v -> attemptRegister());
        binding.buttonGeneratePassword.setOnClickListener(v ->
                binding.inputPassword.setText(generateStrongPassword()));

        binding.genderToggle.check(R.id.button_gender_male);
        binding.militaryToggle.check(R.id.button_military_no);
        binding.genderToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean male = checkedId == R.id.button_gender_male;
            binding.layoutMilitaryChoice.setVisibility(male ? View.VISIBLE : View.GONE);
            if (!male) {
                binding.militaryToggle.check(R.id.button_military_no);
            }
        });
        binding.layoutMilitaryChoice.setVisibility(View.VISIBLE);
    }

    private void attemptRegister() {
        CharSequence fullName = binding.inputFullName.getText();
        CharSequence email = binding.inputEmail.getText();
        CharSequence pass = binding.inputPassword.getText();
        if (fullName == null || fullName.toString().trim().isEmpty()) {
            binding.layoutFullName.setError(getString(R.string.register_name_required));
            return;
        }
        binding.layoutFullName.setError(null);
        if (email == null || email.toString().trim().isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.login_error_empty));
            return;
        }
        binding.layoutEmail.setError(null);
        if (pass == null || pass.toString().trim().isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.login_error_empty));
            return;
        }
        String password = pass.toString();
        if (!isStrongPassword(password)) {
            binding.layoutPassword.setError(getString(R.string.register_password_weak));
            return;
        }
        binding.layoutPassword.setError(null);
        Gender gender = binding.genderToggle.getCheckedButtonId() == R.id.button_gender_female
                ? Gender.FEMALE : Gender.MALE;
        boolean militaryLiable = gender == Gender.MALE
                && binding.militaryToggle.getCheckedButtonId() == R.id.button_military_yes;
        viewModel.register(fullName.toString().trim(), email.toString().trim(), password,
                gender, militaryLiable);
    }

    private static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private static String generateStrongPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String specials = "!@#$%^&*";
        String all = upper + lower + digits + specials;
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt((int) (Math.random() * upper.length())));
        sb.append(lower.charAt((int) (Math.random() * lower.length())));
        sb.append(digits.charAt((int) (Math.random() * digits.length())));
        sb.append(specials.charAt((int) (Math.random() * specials.length())));
        for (int i = 0; i < 8; i++) {
            sb.append(all.charAt((int) (Math.random() * all.length())));
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
