package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;

/**
 * Описание одного поля формы (в ТЗ: RequestField) для типа заявки {@link RequestKindEntity}.
 */
@Entity(
        tableName = "request_fields",
        foreignKeys = @ForeignKey(
                entity = RequestKindEntity.class,
                parentColumns = "id",
                childColumns = "kindId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("kindId"), @Index(value = {"kindId", "fieldKey"}, unique = true)}
)
public class RequestFieldDefinitionEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String kindId = "";

    /** Ключ в JSON payload заявки (например startDate). */
    @NonNull
    public String fieldKey = "";

    @NonNull
    public String label = "";

    @NonNull
    public FieldInputType inputType = FieldInputType.TEXT;

    public boolean required;

    public int sortOrder;

    /** Доп. правила (маска, maxLength, mime для FILE) в виде JSON. */
    public String validationJson;
}
