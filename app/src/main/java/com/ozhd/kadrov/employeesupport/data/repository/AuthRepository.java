package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ozhd.kadrov.employeesupport.BuildConfig;
import com.ozhd.kadrov.employeesupport.core.DemoAccounts;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.remote.RetrofitModule;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginRequest;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginResponse;
import com.ozhd.kadrov.employeesupport.data.remote.dto.UserDto;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseAuthHelper;

import java.io.IOException;
import java.util.UUID;

import retrofit2.Response;

/**
 * Репозиторий авторизации: REST + опционально Firebase Auth + кэш пользователя в Room.
 */
public class AuthRepository {

    private final SessionManager sessionManager;
    private final AppDatabase database;

    public AuthRepository(@NonNull Context context) {
        this.sessionManager = new SessionManager(context);
        this.database = AppDatabase.getInstance(context);
    }

    @NonNull
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Вход: сначала REST; при успехе — пробуем Firebase для realtime-функций;
     * при недоступности API в debug — демо-режим для разработки UI.
     */
    public AuthResult login(@NonNull String email, @NonNull String password) {
        try {
            Response<LoginResponse> response = RetrofitModule.apiService()
                    .login(new LoginRequest(email, password))
                    .execute();

            if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                return finishSuccessfulLogin(response.body(), email, password);
            }

            String err = "Ошибка сервера";
            try {
                if (response.errorBody() != null) {
                    err = response.errorBody().string();
                }
            } catch (IOException ignored) {
                err = "Ошибка сервера (" + response.code() + ")";
            }
            if (BuildConfig.DEBUG && isUnreachableResponse(response.code())) {
                return demoLogin(email, password);
            }
            return AuthResult.error(err);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                return demoLogin(email, password);
            }
            return AuthResult.error("Нет сети или сервер недоступен");
        }
    }

    /** Эндпоинт отсутствует или сервер недоступен — в debug включаем демо-вход. */
    private static boolean isUnreachableResponse(int code) {
        return code >= 500 || code == 404;
    }

    /**
     * Демо: только для отладки без бэкенда. HR — если email содержит "hr." или "admin".
     */
    private AuthResult demoLogin(String email, String password) {
        String e = email.trim();
        String p = password;
        if (e.isEmpty() || p.isEmpty()) {
            return AuthResult.error("Заполните email и пароль");
        }

        UserDto dto = new UserDto();
        dto.email = e;

        if (DemoAccounts.isEmployeeLogin(e, p)) {
            dto.id = DemoAccounts.SEED_USER_EMPLOYEE_ID;
            dto.fullName = "Иванов Иван (сотрудник)";
            dto.role = "EMPLOYEE";
            dto.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        } else if (DemoAccounts.isHrLogin(e, p)) {
            dto.id = DemoAccounts.SEED_USER_HR_ID;
            dto.fullName = "Петрова Мария (HR)";
            dto.role = "HR";
            dto.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        } else {
            dto.id = "demo-" + UUID.randomUUID();
            dto.fullName = "Демо пользователь";
            dto.role = (e.contains("hr.") || e.contains("admin")) ? "HR" : "EMPLOYEE";
        }

        dto.isActive = true;
        dto.updatedAt = System.currentTimeMillis();

        LoginResponse fake = new LoginResponse();
        fake.accessToken = "demo-token-" + dto.id;
        fake.user = dto;
        return finishSuccessfulLogin(fake, e, p);
    }

    private AuthResult finishSuccessfulLogin(@NonNull LoginResponse body, String email, String password) {
        UserDto dto = body.user;
        UserEntity entity = mapUser(dto);
        AppDatabase.dbExecutor.execute(() -> database.userDao().upsert(entity));

        UserRole role = UserRole.fromString(dto.role);
        String token = body.accessToken != null ? body.accessToken : "token-" + entity.id;
        sessionManager.saveSession(token, entity.id, role, dto.email);

        FirebaseAuthHelper.signInEmailPasswordSafe(email, password);

        return AuthResult.success(role);
    }

    private static UserEntity mapUser(UserDto dto) {
        UserEntity u = new UserEntity();
        u.id = dto.id != null ? dto.id : UUID.randomUUID().toString();
        u.email = dto.email != null ? dto.email : "";
        u.fullName = dto.fullName != null ? dto.fullName : "";
        u.phone = dto.phone;
        u.avatarUrl = dto.avatarUrl;
        u.departmentId = dto.departmentId;
        u.role = UserRole.fromString(dto.role);
        u.isActive = dto.isActive;
        u.updatedAt = dto.updatedAt;
        u.isSynced = true;
        return u;
    }

    public void logout() {
        sessionManager.clear();
        FirebaseAuthHelper.signOut();
    }

    public static final class AuthResult {
        public final boolean success;
        @Nullable
        public final UserRole role;
        @Nullable
        public final String errorMessage;

        private AuthResult(boolean success, @Nullable UserRole role, @Nullable String errorMessage) {
            this.success = success;
            this.role = role;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(@NonNull UserRole role) {
            return new AuthResult(true, role, null);
        }

        public static AuthResult error(@NonNull String message) {
            return new AuthResult(false, null, message);
        }
    }
}
