package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO типа заявки (в ТЗ — RequestType): имя, описание, JSON-схема полей, порядок сортировки.
 */
public class RequestKindDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("sortOrder")
    public int sortOrder;

    @SerializedName("isActive")
    public boolean isActive;

    /** См. {@link com.ozhd.kadrov.employeesupport.core.FieldSchemaJson}. */
    @SerializedName("fieldSchemaJson")
    public String fieldSchemaJson;

    @SerializedName("updatedAt")
    public long updatedAt;
}
