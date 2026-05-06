package com.ozhd.kadrov.employeesupport.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.databinding.ActivityLoginBinding;
import com.ozhd.kadrov.employeesupport.ui.main.MainActivity;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.LoginViewModel;

/**
 * Авторизация: валидация полей, вызов репозитория (REST), сохранение сессии во ViewModel.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getState().observe(this, state -> {
            switch (state.phase) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.buttonLogin.setEnabled(false);
                    binding.buttonOpenRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    if (binding == null || isFinishing()) {
                        break;
                    }
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonLogin.setEnabled(true);
                    binding.buttonOpenRegister.setEnabled(true);
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonLogin.setEnabled(true);
                    binding.buttonOpenRegister.setEnabled(true);
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
                    break;
                case IDLE:
                default:
                    binding.progress.setVisibility(View.GONE);
                    binding.buttonLogin.setEnabled(true);
                    binding.buttonOpenRegister.setEnabled(true);
                    break;
            }
        });

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.buttonOpenRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        CharSequence email = binding.inputEmail.getText();
        CharSequence pass = binding.inputPassword.getText();
        if (email == null || email.toString().trim().isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.login_error_empty));
            return;
        }
        binding.layoutEmail.setError(null);
        if (pass == null || pass.toString().trim().isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.login_error_empty));
            return;
        }
        binding.layoutPassword.setError(null);
        viewModel.login(email.toString().trim(), pass.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
