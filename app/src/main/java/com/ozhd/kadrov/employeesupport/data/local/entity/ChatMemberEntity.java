package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Связь многие-ко-многим: участники чата.
 */
@Entity(
        tableName = "chat_members",
        primaryKeys = {"chatId", "userId"},
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
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("userId")}
)
public class ChatMemberEntity {

    @NonNull
    public String chatId = "";

    @NonNull
    public String userId = "";
}
