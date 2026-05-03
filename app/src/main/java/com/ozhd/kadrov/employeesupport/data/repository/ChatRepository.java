package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;
import com.ozhd.kadrov.employeesupport.data.local.pojo.MessageWithSender;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseInitHelper;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreChatSync;

import java.util.List;
import java.util.UUID;

/**
 * Чат: Room — источник для UI; Firestore — при доступном Firebase.
 */
public class ChatRepository {

    private final AppDatabase db;
    private final FirestoreChatSync firestoreSync;

    public ChatRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
        this.firestoreSync = new FirestoreChatSync(db);
    }

    public LiveData<List<MessageEntity>> observeMessages(@NonNull String chatId) {
        return db.chatDao().observeMessages(chatId);
    }

    /** Сообщения с именем отправителя (JOIN users). */
    public LiveData<List<MessageWithSender>> observeMessagesWithSender(@NonNull String chatId) {
        return db.chatDao().observeMessagesWithSender(chatId);
    }

    public void ensureLocalChat(@NonNull String chatId, @NonNull String title, boolean isGroup) {
        AppDatabase.dbExecutor.execute(() -> {
            ChatEntity c = new ChatEntity();
            c.id = chatId;
            c.name = title;
            c.isGroup = isGroup;
            db.chatDao().upsertChat(c);
        });
    }

    @Nullable
    public ListenerRegistration attachRealtimeSync(@NonNull String chatId) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        return firestoreSync.listenMessages(chatId, () -> { });
    }

    public void sendMessage(@NonNull String chatId, @NonNull String senderId, @NonNull String text) {
        if (FirebaseInitHelper.isDefaultAppReady()) {
            firestoreSync.sendMessage(chatId, senderId, text);
        } else {
            AppDatabase.dbExecutor.execute(() -> {
                MessageEntity local = new MessageEntity();
                local.id = UUID.randomUUID().toString();
                local.chatId = chatId;
                local.senderId = senderId;
                local.text = text;
                local.timestamp = System.currentTimeMillis();
                local.isRead = false;
                local.isSynced = false;
                db.chatDao().upsertMessage(local);
            });
        }
    }
}
