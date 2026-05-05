package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;

/**
 * Заявка: привязана к динамическому типу {@link RequestKindEntity}.
 * Значения полей формы — JSON в {@link #payloadJson} (ключи = fieldKey из {@link RequestFieldDefinitionEntity}).
 */
@Entity(
        tableName = "requests",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = RequestKindEntity.class,
                        parentColumns = "id",
                        childColumns = "kindId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {@Index("userId"), @Index("kindId"), @Index("status"), @Index("createdAt")}
)
public class RequestEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String userId = "";

    /** FK на динамический тип; может стать null если HR удалил тип (архив). */
    public String kindId;

    @NonNull
    public RequestStatus status = RequestStatus.PENDING;

    @NonNull
    public String title = "";

    public String description;

    /** Ответы по динамическим полям: {"startDate":"2026-05-01","comment":"..."} */
    public String payloadJson;

    /** Причина отказа (обязательно заполняется HR при статусе REJECTED). */
    public String rejectionReason;

    public long createdAt;
    public long updatedAt;
    public boolean isSynced;
}
