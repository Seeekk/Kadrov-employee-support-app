package com.ozhd.kadrov.employeesupport.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Тело запроса REST-авторизации (адаптируйте поля под ваш бэкенд).
 */
public class LoginRequest {

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
