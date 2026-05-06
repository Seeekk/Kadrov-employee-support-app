package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.repository.AuthRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MVVM: состояние экрана входа. Сеть выполняется не на главном потоке (Retrofit {@code execute}).
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<LoginUiState> state = new MutableLiveData<>(LoginUiState.idle());
    private final ExecutorService loginExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "auth-login");
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    });

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    @Override
    protected void onCleared() {
        loginExecutor.shutdownNow();
        super.onCleared();
    }

    public LiveData<LoginUiState> getState() {
        return state;
    }

    public void login(@NonNull String email, @NonNull String password) {
        state.postValue(LoginUiState.loading());
        loginExecutor.execute(() -> {
            AuthRepository.AuthResult result = authRepository.login(email.trim(), password);
            if (result == null) {
                state.postValue(LoginUiState.error("Ошибка входа"));
                return;
            }
            if (result.success && result.role != null) {
                state.postValue(LoginUiState.success(result.role));
            } else {
                state.postValue(LoginUiState.error(
                        result.errorMessage != null ? result.errorMessage : "Ошибка входа"));
            }
        });
    }

    public void register(
            @NonNull String fullName,
            @NonNull String email,
            @NonNull String password,
            @NonNull Gender gender,
            boolean militaryLiable
    ) {
        state.postValue(LoginUiState.loading());
        loginExecutor.execute(() -> {
            AuthRepository.AuthResult result = authRepository.registerEmployee(
                    fullName.trim(), email.trim(), password, gender, militaryLiable);
            if (result == null) {
                state.postValue(LoginUiState.error("Ошибка регистрации"));
                return;
            }
            if (result.success && result.role != null) {
                state.postValue(LoginUiState.success(result.role));
            } else {
                state.postValue(LoginUiState.error(
                        result.errorMessage != null ? result.errorMessage : "Ошибка регистрации"));
            }
        });
    }

    public static final class LoginUiState {
        public enum Phase { IDLE, LOADING, SUCCESS, ERROR }

        public final Phase phase;
        @NonNull
        public final String message;
        public final UserRole role;

        private LoginUiState(Phase phase, @NonNull String message, UserRole role) {
            this.phase = phase;
            this.message = message;
            this.role = role;
        }

        public static LoginUiState idle() {
            return new LoginUiState(Phase.IDLE, "", null);
        }

        public static LoginUiState loading() {
            return new LoginUiState(Phase.LOADING, "", null);
        }

        public static LoginUiState success(@NonNull UserRole role) {
            return new LoginUiState(Phase.SUCCESS, "", role);
        }

        public static LoginUiState error(@NonNull String msg) {
            return new LoginUiState(Phase.ERROR, msg, null);
        }
    }
}
