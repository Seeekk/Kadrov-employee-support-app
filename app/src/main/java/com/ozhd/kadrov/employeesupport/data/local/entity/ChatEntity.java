package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Чат (личный или групповой).
 */
@Entity(tableName = "chats")
public class ChatEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String name = "";

    public boolean isGroup;
}
