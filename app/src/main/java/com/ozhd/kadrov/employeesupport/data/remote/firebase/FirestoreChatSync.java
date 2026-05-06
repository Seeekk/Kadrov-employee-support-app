package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Realtime Firestore. Если Firebase не готов, чат продолжает работать локально через Room.
 */
public class FirestoreChatSync {

    private static final String TAG = "FirestoreChatSync";

    private final AppDatabase database;

    public FirestoreChatSync(@NonNull AppDatabase database) {
        this.database = database;
    }

    /**
     * Гарантирует строку чата до вставки сообщений (FK messages.chatId → chats.id).
     */
    private void ensureChatRow(@NonNull String chatId, @NonNull String chatTitle) {
        ChatEntity chat = new ChatEntity();
        chat.id = chatId;
        chat.name = chatTitle;
        chat.isGroup = true;
        database.chatDao().upsertChat(chat);
    }

    @Nullable
    public ListenerRegistration listenMessages(
            @NonNull String remoteChatId,
            @NonNull String localChatId,
            @NonNull String chatTitle,
            @NonNull Runnable onChange
    ) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            return firestore.collection("chats")
                    .document(remoteChatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null) {
                            Log.e(TAG, "messages listener failed for chat " + remoteChatId, error);
                            return;
                        }
                        if (snapshot == null) {
                            return;
                        }
                        AppDatabase.dbExecutor.execute(() -> {
                            try {
                                ensureChatRow(localChatId, chatTitle);
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    Map<String, Object> data = doc.getData();
                                    if (data == null) {
                                        continue;
                                    }
                                    MessageEntity msg = new MessageEntity();
                                    msg.id = doc.getId();
                                    msg.chatId = localChatId;
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
                                    ensureLocalUser(msg.senderId);
                                    try {
                                        database.chatDao().upsertMessage(msg);
                                    } catch (Exception ex) {
                                        Log.w(TAG, "skip bad message " + msg.id, ex);
                                    }
                                }
                                onChange.run();
                            } catch (Throwable t) {
                                Log.e(TAG, "persist chat messages failed", t);
                            }
                        });
                    });
        } catch (Exception e) {
            return null;
        }
    }

    public void sendMessage(
            @NonNull String chatId,
            @NonNull String chatTitle,
            @NonNull String senderId,
            @NonNull String text
    ) {
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
                        try {
                            ensureChatRow(chatId, chatTitle);
                            MessageEntity local = new MessageEntity();
                            local.id = id;
                            local.chatId = chatId;
                            local.senderId = senderId;
                            local.text = text;
                            local.timestamp = System.currentTimeMillis();
                            local.isSynced = true;
                            ensureLocalUser(local.senderId);
                            database.chatDao().upsertMessage(local);
                        } catch (Throwable t) {
                            Log.e(TAG, "persist sent message failed", t);
                        }
                    }));
        } catch (Exception ignored) {
            // Локальная копия все равно добавляется в ChatRepository
        }
    }

    private void ensureLocalUser(@NonNull String userId) {
        if (userId.isEmpty()) {
            return;
        }
        UserEntity existing = database.userDao().getUserSync(userId);
        if (existing != null) {
            return;
        }
        UserEntity placeholder = new UserEntity();
        placeholder.id = userId;
        placeholder.email = userId + "@firebase.local";
        placeholder.fullName = "Пользователь Firebase";
        placeholder.role = UserRole.EMPLOYEE;
        placeholder.approvalStatus = ApprovalStatus.APPROVED;
        placeholder.isMilitaryLiable = false;
        placeholder.isActive = true;
        placeholder.updatedAt = System.currentTimeMillis();
        placeholder.isSynced = true;
        database.userDao().upsert(placeholder);
    }
}
