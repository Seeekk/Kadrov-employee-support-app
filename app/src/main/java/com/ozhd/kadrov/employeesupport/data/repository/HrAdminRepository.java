package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;

import java.util.List;

/**
 * CRUD для HR: отделы, сотрудники, типы заявок и поля (локальный слой Room).
 * Загрузка снимка с сервера — {@link HrCatalogSyncRepository#pullAndApplyBlocking()}.
 */
public class HrAdminRepository {

    private final AppDatabase db;

    public HrAdminRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
    }

    public LiveData<List<DepartmentEntity>> observeDepartments() {
        return db.departmentDao().observeAll();
    }

    public void upsertDepartment(@NonNull DepartmentEntity entity) {
        AppDatabase.dbExecutor.execute(() -> db.departmentDao().upsert(entity));
    }

    public void deleteDepartment(@NonNull String id) {
        AppDatabase.dbExecutor.execute(() -> db.departmentDao().deleteById(id));
    }

    public LiveData<List<UserEntity>> observeAllUsers() {
        return db.userDao().observeAllUsers();
    }

    public void upsertUser(@NonNull UserEntity user) {
        AppDatabase.dbExecutor.execute(() -> db.userDao().upsert(user));
    }

    public void deleteUser(@NonNull String userId) {
        AppDatabase.dbExecutor.execute(() -> db.userDao().deleteById(userId));
    }

    public LiveData<List<RequestKindEntity>> observeAllKinds() {
        return db.requestKindDao().observeAllKinds();
    }

    public void upsertRequestKind(@NonNull RequestKindEntity kind) {
        AppDatabase.dbExecutor.execute(() -> db.requestKindDao().upsert(kind));
    }

    public void deleteRequestKind(@NonNull String kindId) {
        AppDatabase.dbExecutor.execute(() -> {
            db.requestFieldDao().deleteForKind(kindId);
            db.requestKindDao().deleteById(kindId);
        });
    }

    public void upsertField(@NonNull RequestFieldDefinitionEntity field) {
        AppDatabase.dbExecutor.execute(() -> db.requestFieldDao().upsert(field));
    }

    public void deleteField(@NonNull String fieldId) {
        AppDatabase.dbExecutor.execute(() -> db.requestFieldDao().deleteById(fieldId));
    }

    public void computeNextKindSortOrder(@NonNull Callback<Integer> callback) {
        AppDatabase.dbExecutor.execute(() ->
                callback.onResult(db.requestKindDao().maxSortOrder() + 10));
    }

    public interface Callback<T> {
        void onResult(T value);
    }
}
