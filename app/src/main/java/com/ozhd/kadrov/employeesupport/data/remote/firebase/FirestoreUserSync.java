package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Синхронизация карточки пользователя в Firestore.
 */
public class FirestoreUserSync {

    @Nullable
    private final AppDatabase database;

    public FirestoreUserSync() {
        this.database = null;
    }

    public FirestoreUserSync(@NonNull AppDatabase database) {
        this.database = database;
    }

    public void pushUser(@NonNull UserEntity user) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return;
        }
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.id);
            data.put("email", user.email);
            data.put("fullName", user.fullName);
            data.put("phone", user.phone);
            data.put("avatarUrl", user.avatarUrl);
            data.put("departmentId", user.departmentId);
            data.put("position", user.position);
            data.put("gender", user.gender != null ? user.gender.name() : null);
            data.put("militaryDocument", user.militaryDocument);
            data.put("role", user.role != null ? user.role.name() : null);
            data.put("approvalStatus", user.approvalStatus != null ? user.approvalStatus.name() : null);
            data.put("isMilitaryLiable", user.isMilitaryLiable);
            data.put("isActive", user.isActive);
            data.put("dismissalReason", user.dismissalReason);
            data.put("updatedAt", user.updatedAt);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.id)
                    .set(data);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public ListenerRegistration listenAllUsers() {
        if (!FirebaseInitHelper.isDefaultAppReady() || database == null) {
            return null;
        }
        try {
            return FirebaseFirestore.getInstance()
                    .collection("users")
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshot, error) -> {
                        if (snapshot == null || error != null) {
                            return;
                        }
                        AppDatabase.dbExecutor.execute(() -> {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Map<String, Object> data = doc.getData();
                                if (data == null) {
                                    continue;
                                }
                                UserEntity user = fromSnapshot(doc.getId(), data);
                                database.userDao().upsert(user);
                            }
                        });
                    });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Читает карточку пользователя из документа Firestore (например для входа без REST).
     */
    @Nullable
    public static UserEntity userEntityFromFirestoreDoc(@Nullable DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }
        Map<String, Object> data = doc.getData();
        if (data == null) {
            return null;
        }
        return fromSnapshot(doc.getId(), data);
    }

    @NonNull
    private static UserEntity fromSnapshot(@NonNull String docId, @NonNull Map<String, Object> data) {
        UserEntity user = new UserEntity();
        user.id = safeString(data.get("id"), docId);
        user.email = safeString(data.get("email"), docId + "@local.invalid");
        user.passwordHash = null;
        user.fullName = safeString(data.get("fullName"), "Пользователь");
        user.phone = nullableString(data.get("phone"));
        user.avatarUrl = nullableString(data.get("avatarUrl"));
        user.departmentId = nullableString(data.get("departmentId"));
        user.position = nullableString(data.get("position"));
        user.gender = Gender.fromString(nullableString(data.get("gender")));
        user.militaryDocument = nullableString(data.get("militaryDocument"));
        user.role = UserRole.fromString(nullableString(data.get("role")));
        String appr = nullableString(data.get("approvalStatus"));
        if (appr != null) {
            appr = appr.trim();
        }
        user.approvalStatus = ApprovalStatus.fromString(appr);
        user.isMilitaryLiable = parseFirestoreBool(data.get("isMilitaryLiable"));
        user.isActive = parseFirestoreBool(data.get("isActive"));
        user.dismissalReason = nullableString(data.get("dismissalReason"));
        user.updatedAt = safeLong(data.get("updatedAt"));
        user.isSynced = true;
        return user;
    }

    @NonNull
    private static String safeString(@Nullable Object value, @NonNull String fallback) {
        if (value == null) {
            return fallback;
        }
        String parsed = String.valueOf(value);
        return parsed.isEmpty() ? fallback : parsed;
    }

    @Nullable
    private static String nullableString(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        String parsed = String.valueOf(value);
        return parsed.isEmpty() ? null : parsed;
    }

    private static long safeLong(@Nullable Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return System.currentTimeMillis();
    }

    /** Firestore / консоль иногда возвращают строку {@code "true"} вместо boolean. */
    private static boolean parseFirestoreBool(@Nullable Object value) {
        if (value == null) {
            return false;
        }
        if (Boolean.TRUE.equals(value)) {
            return true;
        }
        if (value instanceof Number && ((Number) value).intValue() != 0) {
            return true;
        }
        String s = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        return "true".equals(s) || "1".equals(s) || "yes".equals(s);
    }
}
