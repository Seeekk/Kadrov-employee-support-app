package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;

import java.util.Map;
import java.util.UUID;

/**
 * Отправка заявки сотрудником: сохранение в Room + заготовка под синхронизацию с API.
 */
public class RequestSubmissionRepository {

    private final AppDatabase db;
    private final Gson gson = new Gson();

    public RequestSubmissionRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    public void submitAsync(@NonNull String userId,
                            @NonNull String kindId,
                            @NonNull String title,
                            @NonNull String description,
                            @NonNull Map<String, String> fieldValues) {
        AppDatabase.dbExecutor.execute(() -> {
            JsonObject payload = new JsonObject();
            for (Map.Entry<String, String> e : fieldValues.entrySet()) {
                if (e.getValue() != null) {
                    payload.addProperty(e.getKey(), e.getValue());
                }
            }
            RequestEntity r = new RequestEntity();
            r.id = UUID.randomUUID().toString();
            r.userId = userId;
            r.kindId = kindId;
            r.status = RequestStatus.PENDING;
            r.title = title;
            r.description = description.isEmpty() ? null : description;
            r.payloadJson = gson.toJson(payload);
            r.createdAt = System.currentTimeMillis();
            r.updatedAt = r.createdAt;
            r.isSynced = false;
            db.requestDao().upsert(r);
        });
    }
}
