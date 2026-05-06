package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.model.NotificationType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirestoreNotificationSync {

    private final AppDatabase database;

    public FirestoreNotificationSync(@NonNull AppDatabase database) {
        this.database = database;
    }

    public void sendRegistrationNotification(
            @NonNull String hrUserId,
            @NonNull String newUserId,
            @NonNull String title,
            @NonNull String message
    ) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return;
        }
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String id = UUID.randomUUID().toString();
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", NotificationType.REGISTRATION.name());
            payload.put("title", title);
            payload.put("message", message);
            payload.put("relatedUserId", newUserId);
            payload.put("createdAt", System.currentTimeMillis());
            payload.put("isRead", false);
            firestore.collection("notifications")
                    .document(hrUserId)
                    .collection("items")
                    .document(id)
                    .set(payload);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public ListenerRegistration listen(@NonNull String userId) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            return firestore.collection("notifications")
                    .document(userId)
                    .collection("items")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshot, error) -> {
                        if (snapshot == null || error != null) {
                            return;
                        }
                        AppDatabase.dbExecutor.execute(() -> {
                            try {
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    Map<String, Object> data = doc.getData();
                                    if (data == null) continue;
                                    NotificationEntity e = new NotificationEntity();
                                    e.id = "fs-" + doc.getId();
                                    e.userId = userId;
                                    e.type = NotificationType.fromString(String.valueOf(data.get("type")));
                                    e.title = String.valueOf(data.get("title"));
                                    e.message = String.valueOf(data.get("message"));
                                    Object rel = data.get("relatedUserId");
                                    e.relatedUserId = rel != null ? rel.toString() : null;
                                    Object ts = data.get("createdAt");
                                    e.createdAt = ts instanceof Number ? ((Number) ts).longValue() : System.currentTimeMillis();
                                    e.isRead = Boolean.TRUE.equals(data.get("isRead"));
                                    database.notificationDao().upsert(e);
                                }
                            } catch (Exception ignored) {
                                // Не роняем процесс, если локальная БД отклонила одну из записей.
                            }
                        });
                    });
        } catch (Exception e) {
            return null;
        }
    }
}
