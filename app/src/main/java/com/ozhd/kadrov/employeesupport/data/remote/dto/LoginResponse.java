package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Ответ сервера после успешного входа.
 */
public class LoginResponse {

    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("user")
    public UserDto user;
}
