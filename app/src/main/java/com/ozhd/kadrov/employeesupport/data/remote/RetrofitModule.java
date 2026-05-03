package com.ozhd.kadrov.employeesupport.data.remote;

import android.app.Application;

import androidx.annotation.Nullable;

import com.ozhd.kadrov.employeesupport.BuildConfig;
import com.ozhd.kadrov.employeesupport.core.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Фабрика Retrofit. Логирование включено только в debug.
 * <p>
 * Вызовите {@link #init(Application)} из {@link android.app.Application#onCreate()}
 * до первого сетевого запроса, чтобы к HR-эндпоинтам подставлялся Bearer-токен из сессии.
 */
public final class RetrofitModule {

    private static volatile ApiService apiService;
    @Nullable
    private static volatile Application appContext;

    private RetrofitModule() {
    }

    /** Инициализация контекста приложения для {@link SessionManager} в интерцепторе авторизации. */
    public static void init(@Nullable Application application) {
        appContext = application;
    }

    private static Interceptor authInterceptor() {
        return chain -> {
            Request.Builder b = chain.request().newBuilder();
            Application app = appContext;
            if (app != null) {
                String token = new SessionManager(app).getAccessToken();
                if (token != null && !token.isEmpty()) {
                    b.header("Authorization", "Bearer " + token);
                }
            }
            return chain.proceed(b.build());
        };
    }

    public static ApiService apiService() {
        if (apiService == null) {
            synchronized (RetrofitModule.class) {
                if (apiService == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(authInterceptor())
                            .addInterceptor(logging)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}
