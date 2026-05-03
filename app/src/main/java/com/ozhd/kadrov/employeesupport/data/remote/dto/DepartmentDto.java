package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** DTO отдела в ответе {@link HrCatalogResponse}. */
public class DepartmentDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;
}
