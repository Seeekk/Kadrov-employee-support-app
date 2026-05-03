package com.ozhd.kadrov.employeesupport.ui.auth;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.repository.AuthRepository;

/**
 * Единая точка выхода: сброс сессии и переход на экран входа без возврата «Назад» в приложение.
 */
public final class AuthUiHelper {

    private AuthUiHelper() {
    }

    /** Диалог подтверждения и выход (из меню или экрана профиля). */
    public static void showLogoutDialog(@NonNull FragmentActivity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.logout_confirm_yes, (dialog, which) -> logout(activity))
                .show();
    }

    public static void logout(@NonNull Activity activity) {
        new AuthRepository(activity).logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
