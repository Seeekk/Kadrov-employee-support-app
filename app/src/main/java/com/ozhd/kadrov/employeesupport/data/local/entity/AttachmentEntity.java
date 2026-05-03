package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Вложение к заявке.
 */
@Entity(
        tableName = "attachments",
        foreignKeys = @ForeignKey(
                entity = RequestEntity.class,
                parentColumns = "id",
                childColumns = "requestId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("requestId")}
)
public class AttachmentEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String requestId = "";

    @NonNull
    public String fileUrl = "";

    public String fileType;
}
