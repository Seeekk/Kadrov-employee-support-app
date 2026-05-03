package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ozhd.kadrov.employeesupport.data.model.NotificationType;

/**
 * Уведомление внутри приложения.
 */
@Entity(
        tableName = "notifications",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId"), @Index("isRead"), @Index("createdAt")}
)
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String userId = "";

    @NonNull
    public NotificationType type = NotificationType.SYSTEM;

    @NonNull
    public String title = "";

    @NonNull
    public String message = "";

    public boolean isRead;
    public long createdAt;
}
