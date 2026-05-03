package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Сообщение чата (offline-first + синхронизация с Firebase).
 */
@Entity(
        tableName = "messages",
        foreignKeys = {
                @ForeignKey(
                        entity = ChatEntity.class,
                        parentColumns = "id",
                        childColumns = "chatId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "senderId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("chatId"), @Index("timestamp"), @Index("senderId")}
)
public class MessageEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String chatId = "";

    @NonNull
    public String senderId = "";

    @NonNull
    public String text = "";

    public long timestamp;
    public boolean isRead;
    public boolean isSynced;
}
