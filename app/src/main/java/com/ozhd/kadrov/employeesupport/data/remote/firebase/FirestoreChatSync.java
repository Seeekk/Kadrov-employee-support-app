package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Realtime Firestore. Вызовы {@link FirebaseFirestore#getInstance()} только после
 * {@link FirebaseInitHelper#isDefaultAppReady()}, иначе чат остаётся только в Room.
 */
public class FirestoreChatSync {

    private final AppDatabase database;

    public FirestoreChatSync(@NonNull AppDatabase database) {
        this.database = database;
    }

    @Nullable
    public ListenerRegistration listenMessages(@NonNull String chatId, @NonNull Runnable onChange) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            return firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshot, error) -> {
                        if (snapshot == null || error != null) {
                            return;
                        }
                        AppDatabase.dbExecutor.execute(() -> {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Map<String, Object> data = doc.getData();
                                if (data == null) continue;
                                MessageEntity msg = new MessageEntity();
                                msg.id = doc.getId();
                                msg.chatId = chatId;
                                Object sid = data.get("senderId");
                                msg.senderId = sid != null ? sid.toString() : "";
                                Object text = data.get("text");
                                msg.text = text != null ? text.toString() : "";
                                Object ts = data.get("timestamp");
                                msg.timestamp = ts instanceof Number
                                        ? ((Number) ts).longValue()
                                        : System.currentTimeMillis();
                                msg.isRead = Boolean.TRUE.equals(data.get("isRead"));
                                msg.isSynced = true;
                                database.chatDao().upsertMessage(msg);
                            }
                            onChange.run();
                        });
                    });
        } catch (Exception e) {
            return null;
        }
    }

    public void sendMessage(@NonNull String chatId, @NonNull String senderId, @NonNull String text) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return;
        }
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String id = UUID.randomUUID().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("senderId", senderId);
            map.put("text", text);
            map.put("timestamp", System.currentTimeMillis());
            map.put("isRead", false);

            firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(id)
                    .set(map)
                    .addOnSuccessListener(v -> AppDatabase.dbExecutor.execute(() -> {
                        MessageEntity local = new MessageEntity();
                        local.id = id;
                        local.chatId = chatId;
                        local.senderId = senderId;
                        local.text = text;
                        local.timestamp = System.currentTimeMillis();
                        local.isSynced = true;
                        database.chatDao().upsertMessage(local);
                    }));
        } catch (Exception ignored) {
            // Только Firestore; локальная копия добавляется в ChatRepository
        }
    }
}
