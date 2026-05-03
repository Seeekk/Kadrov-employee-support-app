package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO одного поля динамической формы (в ТЗ — RequestField), нормализованная строка в БД.
 */
public class RequestFieldDto {

    @SerializedName("id")
    public String id;

    @SerializedName("kindId")
    public String kindId;

    @SerializedName("fieldKey")
    public String fieldKey;

    @SerializedName("label")
    public String label;

    /** TEXT, LONG_TEXT, DATE, NUMBER, FILE */
    @SerializedName("inputType")
    public String inputType;

    @SerializedName("required")
    public boolean required;

    @SerializedName("sortOrder")
    public int sortOrder;

    @SerializedName("validationJson")
    public String validationJson;
}
