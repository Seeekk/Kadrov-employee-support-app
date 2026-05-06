package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

/**
 * Обертка над Firebase Auth: безопасные вызовы при отсутствии инициализации.
 */
public final class FirebaseAuthHelper {

    private FirebaseAuthHelper() {
    }

    /** Не блокирует UI; при ошибке Firebase просто пропускается. */
    public static void signInEmailPasswordSafe(@NonNull String email, @NonNull String password) {
        try {
            if (!FirebaseInitHelper.isDefaultAppReady()) {
                return;
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password);
        } catch (Exception ignored) {
        }
    }

    public static void signOut() {
        try {
            if (!FirebaseInitHelper.isDefaultAppReady()) {
                return;
            }
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static String getCurrentUid() {
        try {
            if (!FirebaseInitHelper.isDefaultAppReady()) {
                return null;
            }
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            return u != null ? u.getUid() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Task<AuthResult> signInEmailPassword(@NonNull String email, @NonNull String password) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            return FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Task<AuthResult> createUserEmailPassword(@NonNull String email, @NonNull String password) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password);
        } catch (Exception e) {
            return null;
        }
    }

    public static void syncRoleFromCustomClaims(@NonNull SessionManager sessionManager) {
        try {
            if (!FirebaseInitHelper.isDefaultAppReady()) {
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                return;
            }
            user.getIdToken(false).addOnSuccessListener(result -> {
                Object claim = result.getClaims().get("role");
                if (!(claim instanceof String)) {
                    return;
                }
                String roleClaim = (String) claim;
                UserRole role = UserRole.fromString(roleClaim);
                String token = sessionManager.getAccessToken();
                String userId = sessionManager.getUserIdOrEmpty();
                if (token == null || userId.isEmpty()) {
                    return;
                }
                String email = sessionManager.getEmailOrEmpty();
                sessionManager.saveSession(token, userId, role, email.isEmpty() ? null : email);
            });
        } catch (Exception ignored) {
        }
    }
}
