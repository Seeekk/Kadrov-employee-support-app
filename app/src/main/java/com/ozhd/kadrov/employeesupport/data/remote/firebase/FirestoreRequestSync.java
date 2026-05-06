package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

import java.util.HashMap;
import java.util.Map;

/**
 * Синхронизация заявок с Firestore.
 */
public class FirestoreRequestSync {

    private final AppDatabase database;

    public FirestoreRequestSync(@NonNull AppDatabase database) {
        this.database = database;
    }

    public void sendRequest(@NonNull RequestEntity request) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", request.userId);
            payload.put("kindId", request.kindId);
            payload.put("status", request.status.name());
            payload.put("title", request.title);
            payload.put("description", request.description);
            payload.put("payloadJson", request.payloadJson);
            payload.put("rejectionReason", request.rejectionReason);
            payload.put("createdAt", request.createdAt);
            payload.put("updatedAt", request.updatedAt);
            FirebaseFirestore.getInstance()
                    .collection("hr_requests")
                    .document(request.id)
                    .set(payload);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public ListenerRegistration listenAll() {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            return FirebaseFirestore.getInstance()
                    .collection("hr_requests")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
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
                                RequestEntity request = fromSnapshot(doc, data);
                                ensureUserExists(request.userId);
                                database.requestDao().upsert(request);
                            }
                        });
                    });
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    private static RequestEntity fromSnapshot(@NonNull DocumentSnapshot doc, @NonNull Map<String, Object> data) {
        RequestEntity request = new RequestEntity();
        request.id = doc.getId();
        request.userId = safeString(data.get("userId"));
        request.kindId = nullableString(data.get("kindId"));
        request.status = RequestStatus.fromString(safeString(data.get("status")));
        request.title = safeString(data.get("title"));
        request.description = nullableString(data.get("description"));
        request.payloadJson = nullableString(data.get("payloadJson"));
        request.rejectionReason = nullableString(data.get("rejectionReason"));
        request.createdAt = safeLong(data.get("createdAt"));
        request.updatedAt = safeLong(data.get("updatedAt"));
        request.isSynced = true;
        return request;
    }

    private void ensureUserExists(@NonNull String userId) {
        if (userId.isEmpty()) {
            return;
        }
        UserEntity existing = database.userDao().getUserSync(userId);
        if (existing != null) {
            return;
        }
        UserEntity placeholder = new UserEntity();
        placeholder.id = userId;
        placeholder.email = userId + "@local.invalid";
        placeholder.passwordHash = null;
        placeholder.fullName = "Сотрудник";
        placeholder.phone = null;
        placeholder.avatarUrl = null;
        placeholder.departmentId = null;
        placeholder.position = "";
        placeholder.gender = Gender.MALE;
        placeholder.militaryDocument = null;
        placeholder.role = UserRole.EMPLOYEE;
        placeholder.approvalStatus = ApprovalStatus.APPROVED;
        placeholder.isMilitaryLiable = false;
        placeholder.isActive = true;
        placeholder.updatedAt = System.currentTimeMillis();
        placeholder.isSynced = true;
        database.userDao().upsert(placeholder);
    }

    @NonNull
    private static String safeString(@Nullable Object value) {
        return value == null ? "" : String.valueOf(value);
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
}
