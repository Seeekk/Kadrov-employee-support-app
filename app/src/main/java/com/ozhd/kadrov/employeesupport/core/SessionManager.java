package com.ozhd.kadrov.employeesupport.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ozhd.kadrov.employeesupport.data.model.UserRole;

/**
 * Локальное хранение сессии (токен, id пользователя, роль) для multi-device UX:
 * при следующем запуске Splash перенаправит в Main без повторного ввода пароля,
 * пока токен валиден (проверка на сервере — в фоне через синхронизацию).
 */
public final class SessionManager {

    private static final String PREFS = "kadrov_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public SessionManager(@NonNull Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getString(KEY_TOKEN, null) != null && prefs.getString(KEY_USER_ID, null) != null;
    }

    public void saveSession(@NonNull String accessToken,
                            @NonNull String userId,
                            @NonNull UserRole role,
                            @Nullable String email) {
        prefs.edit()
                .putString(KEY_TOKEN, accessToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role.name())
                .putString(KEY_EMAIL, email)
                .apply();
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @NonNull
    public String getUserIdOrEmpty() {
        String id = prefs.getString(KEY_USER_ID, null);
        return id != null ? id : "";
    }

    @NonNull
    public UserRole getRole() {
        return UserRole.fromString(prefs.getString(KEY_ROLE, UserRole.EMPLOYEE.name()));
    }

    @NonNull
    public String getEmailOrEmpty() {
        String email = prefs.getString(KEY_EMAIL, null);
        return email != null ? email : "";
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
