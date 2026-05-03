package com.ozhd.kadrov.employeesupport.data.remote;

import com.ozhd.kadrov.employeesupport.data.remote.dto.HrCatalogResponse;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginRequest;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Контракт REST API: авторизация и снимок справочников для консоли HR (Room как кэш).
 */
public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    /**
     * Полный каталог для HR: отделы, пользователи, типы заявок и поля.
     * Бэкенд должен вернуть JSON в форме {@link HrCatalogResponse}; при отсутствии сервера
     * синхронизация завершится ошибкой, локальные данные Room остаются без изменений.
     */
    @GET("hr/catalog")
    Call<HrCatalogResponse> getHrCatalog();
}
