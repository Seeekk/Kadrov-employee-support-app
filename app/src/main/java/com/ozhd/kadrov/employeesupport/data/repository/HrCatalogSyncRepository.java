package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.remote.RetrofitModule;
import com.ozhd.kadrov.employeesupport.data.remote.dto.DepartmentDto;
import com.ozhd.kadrov.employeesupport.data.remote.dto.HrCatalogResponse;
import com.ozhd.kadrov.employeesupport.data.remote.dto.RequestFieldDto;
import com.ozhd.kadrov.employeesupport.data.remote.dto.RequestKindDto;
import com.ozhd.kadrov.employeesupport.data.remote.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;

/**
 * Слой синхронизации «сервер → Room» для консоли HR: один GET возвращает согласованный снимок.
 * Локальные правки с флагом {@code isSynced=false} этот класс не перезаписывает (упрощённая политика merge).
 */
public class HrCatalogSyncRepository {

    private final AppDatabase db;

    public HrCatalogSyncRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    /**
     * Синхронный вызов сети + транзакция Room; вызывайте с фонового потока.
     *
     * @return null при успехе, иначе текст ошибки для Toast
     */
    @Nullable
    public String pullAndApplyBlocking() {
        try {
            Response<HrCatalogResponse> resp = RetrofitModule.apiService().getHrCatalog().execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                return "HTTP " + resp.code();
            }
            applyCatalog(resp.body());
            return null;
        } catch (IOException e) {
            return e.getMessage() != null ? e.getMessage() : "IOException";
        }
    }

    private void applyCatalog(@NonNull HrCatalogResponse body) {
        db.runInTransaction(() -> {
            if (body.departments != null) {
                for (DepartmentDto d : body.departments) {
                    if (d == null || d.id == null) {
                        continue;
                    }
                    DepartmentEntity e = new DepartmentEntity();
                    e.id = d.id;
                    e.name = d.name != null ? d.name : "";
                    e.description = d.description;
                    db.departmentDao().upsert(e);
                }
            }
            if (body.users != null) {
                List<UserEntity> batch = new ArrayList<>();
                for (UserDto dto : body.users) {
                    if (dto == null || dto.id == null) {
                        continue;
                    }
                    batch.add(mapUser(dto));
                }
                if (!batch.isEmpty()) {
                    db.userDao().upsertAll(batch);
                }
            }
            if (body.requestKinds != null) {
                for (RequestKindDto k : body.requestKinds) {
                    if (k == null || k.id == null) {
                        continue;
                    }
                    RequestKindEntity e = new RequestKindEntity();
                    e.id = k.id;
                    e.name = k.name != null ? k.name : "";
                    e.description = k.description;
                    e.sortOrder = k.sortOrder;
                    e.isActive = k.isActive;
                    e.fieldSchemaJson = k.fieldSchemaJson;
                    e.updatedAt = k.updatedAt;
                    e.isSynced = true;
                    db.requestKindDao().upsert(e);
                }
            }
            if (body.requestFields != null) {
                for (RequestFieldDto f : body.requestFields) {
                    if (f == null || f.id == null || f.kindId == null) {
                        continue;
                    }
                    RequestFieldDefinitionEntity e = new RequestFieldDefinitionEntity();
                    e.id = f.id;
                    e.kindId = f.kindId;
                    e.fieldKey = f.fieldKey != null ? f.fieldKey : "";
                    e.label = f.label != null ? f.label : "";
                    e.inputType = FieldInputType.fromString(f.inputType);
                    e.required = f.required;
                    e.sortOrder = f.sortOrder;
                    e.validationJson = f.validationJson;
                    db.requestFieldDao().upsert(e);
                }
            }
        });
    }

    private static UserEntity mapUser(@NonNull UserDto dto) {
        UserEntity u = new UserEntity();
        u.id = dto.id != null ? dto.id : UUID.randomUUID().toString();
        u.email = dto.email != null ? dto.email : "";
        u.fullName = dto.fullName != null ? dto.fullName : "";
        u.phone = dto.phone;
        u.avatarUrl = dto.avatarUrl;
        u.departmentId = dto.departmentId;
        u.role = UserRole.fromString(dto.role);
        u.isActive = dto.isActive;
        u.updatedAt = dto.updatedAt;
        u.isSynced = true;
        return u;
    }
}
