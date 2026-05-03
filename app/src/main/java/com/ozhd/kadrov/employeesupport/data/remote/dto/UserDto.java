package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO пользователя с API (маппится в {@link com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity}).
 */
public class UserDto {

    @SerializedName("id")
    public String id;

    @SerializedName("email")
    public String email;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("phone")
    public String phone;

    @SerializedName("avatarUrl")
    public String avatarUrl;

    @SerializedName("departmentId")
    public String departmentId;

    /** Строка роли: EMPLOYEE | HR */
    @SerializedName("role")
    public String role;

    @SerializedName("isActive")
    public boolean isActive;

    @SerializedName("updatedAt")
    public long updatedAt;
}
