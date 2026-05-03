package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Комментарий к заявке (история модерации).
 */
@Entity(
        tableName = "request_comments",
        foreignKeys = {
                @ForeignKey(
                        entity = RequestEntity.class,
                        parentColumns = "id",
                        childColumns = "requestId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("requestId"), @Index("userId")}
)
public class RequestCommentEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String requestId = "";

    @NonNull
    public String userId = "";

    @NonNull
    public String comment = "";

    public long createdAt;
}
