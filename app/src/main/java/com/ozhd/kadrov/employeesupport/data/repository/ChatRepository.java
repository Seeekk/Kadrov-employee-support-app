package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.ozhd.kadrov.employeesupport.core.AppConstants;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;
import com.ozhd.kadrov.employeesupport.data.local.pojo.MessageWithSender;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseInitHelper;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreChatSync;

import java.util.List;
import java.util.UUID;

/**
 * Гибридный чат: Room (локально) + Firestore (удаленная синхронизация).
 */
public class ChatRepository {

    private static final String LEGACY_GLOBAL_CHAT_ID = "global";

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
        String title = resolveChatTitle(chatId);
        ListenerRegistration primary = firestoreSync.listenMessages(chatId, chatId, title, () -> { });

        if (!AppConstants.CHAT_GLOBAL_ID.equals(chatId)) {
            return primary;
        }

        ListenerRegistration legacy = firestoreSync.listenMessages(
                LEGACY_GLOBAL_CHAT_ID,
                chatId,
                title,
                () -> { }
        );

        if (primary == null) {
            return legacy;
        }
        if (legacy == null) {
            return primary;
        }
        return () -> {
            primary.remove();
            legacy.remove();
        };
    }

    @NonNull
    private static String resolveChatTitle(@NonNull String chatId) {
        return AppConstants.CHAT_GLOBAL_ID.equals(chatId) ? "Общий чат" : "Чат";
    }

    public void sendMessage(@NonNull String chatId, @NonNull String senderId, @NonNull String text) {
        String title = resolveChatTitle(chatId);
        if (FirebaseInitHelper.isDefaultAppReady()) {
            firestoreSync.sendMessage(chatId, title, senderId, text);
            return;
        }

        AppDatabase.dbExecutor.execute(() -> {
            ChatEntity chatRow = new ChatEntity();
            chatRow.id = chatId;
            chatRow.name = title;
            chatRow.isGroup = true;
            db.chatDao().upsertChat(chatRow);
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
