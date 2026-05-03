package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.ozhd.kadrov.employeesupport.core.FieldSchemaParser;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;

import java.util.Collections;
import java.util.List;

/**
 * Каталог типов заявок и определений полей (чтение для UI создания заявки и консоли HR).
 */
public class RequestCatalogRepository {

    private final AppDatabase db;

    public RequestCatalogRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<List<RequestKindEntity>> observeActiveKinds() {
        return db.requestKindDao().observeActiveKinds();
    }

    public LiveData<List<RequestKindEntity>> observeAllKinds() {
        return db.requestKindDao().observeAllKinds();
    }

    public LiveData<List<RequestFieldDefinitionEntity>> observeFieldsForKind(@NonNull String kindId) {
        return db.requestFieldDao().observeForKind(kindId);
    }

    /**
     * Поля для формы сотрудника: сначала строки из {@code request_fields}; если пусто — парсинг
     * {@link RequestKindEntity#fieldSchemaJson}, чтобы новые типы заявок работали без правки кода APK.
     */
    @NonNull
    public LiveData<List<RequestFieldDefinitionEntity>> observeResolvedFieldsForKind(
            @NonNull String kindId) {
        MediatorLiveData<List<RequestFieldDefinitionEntity>> out = new MediatorLiveData<>();
        LiveData<List<RequestFieldDefinitionEntity>> fromRows = db.requestFieldDao().observeForKind(kindId);
        LiveData<RequestKindEntity> kindLd = db.requestKindDao().observeKind(kindId);

        Runnable merge = () -> {
            List<RequestFieldDefinitionEntity> rows = fromRows.getValue();
            RequestKindEntity kind = kindLd.getValue();
            if (rows == null || kind == null) {
                return;
            }
            if (!rows.isEmpty()) {
                out.setValue(rows);
                return;
            }
            String raw = kind.fieldSchemaJson != null ? kind.fieldSchemaJson.trim() : "";
            if (!raw.isEmpty() && !"{}".equals(raw)) {
                out.setValue(FieldSchemaParser.parseFieldsFromSchema(kindId, kind.fieldSchemaJson));
            } else {
                out.setValue(Collections.emptyList());
            }
        };

        out.addSource(fromRows, v -> merge.run());
        out.addSource(kindLd, v -> merge.run());
        return out;
    }
}
