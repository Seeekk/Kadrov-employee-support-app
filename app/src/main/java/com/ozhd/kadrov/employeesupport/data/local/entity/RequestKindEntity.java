package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Динамический тип заявки (в ТЗ: RequestType / «тип заявки»). Создаётся HR, синхронизируется с сервером.
 * Поля формы дублируются нормализованно в {@link RequestFieldDefinitionEntity}
 * и опционально JSON-снимком в {@link #fieldSchemaJson} (см. {@link com.ozhd.kadrov.employeesupport.core.FieldSchemaJson}).
 */
@Entity(tableName = "request_kinds")
public class RequestKindEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String name = "";

    public String description;

    public int sortOrder;

    public boolean isActive = true;

    /**
     * Резервная JSON-схема для синхронизации с backend (см. FieldSchemaJson).
     */
    public String fieldSchemaJson;

    public long updatedAt;
    public boolean isSynced;
}
