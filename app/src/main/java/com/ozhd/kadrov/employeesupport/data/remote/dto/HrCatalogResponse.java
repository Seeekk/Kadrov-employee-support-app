package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Снимок справочников с сервера для кэширования в Room (отделы, люди, типы заявок, поля).
 */
public class HrCatalogResponse {

    @SerializedName("departments")
    public List<DepartmentDto> departments;

    @SerializedName("users")
    public List<UserDto> users;

    @SerializedName("requestKinds")
    public List<RequestKindDto> requestKinds;

    @SerializedName("requestFields")
    public List<RequestFieldDto> requestFields;
}
