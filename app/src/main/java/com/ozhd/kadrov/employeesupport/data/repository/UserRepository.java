package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreUserSync;

/**
 * Профиль и списки сотрудников из локального кэша (синхронизация — отдельным SyncWorker/API).
 */
public class UserRepository {

    private final AppDatabase db;
    private final SessionManager sessionManager;
    private final FirestoreUserSync firestoreUserSync;

    public UserRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
        this.sessionManager = new SessionManager(context);
        this.firestoreUserSync = new FirestoreUserSync();
    }

    public LiveData<UserEntity> observeCurrentUser() {
        String id = sessionManager.getUserIdOrEmpty();
        if (id.isEmpty()) {
            return new MutableLiveData<>(null);
        }
        return db.userDao().observeUser(id);
    }

    public LiveData<java.util.List<UserEntity>> observeAllActive() {
        return db.userDao().observeActiveUsers();
    }

    public LiveData<java.util.List<UserEntity>> observeDepartment(@NonNull String departmentId) {
        return db.userDao().observeUsersByDepartment(departmentId);
    }

    /**
     * Пример резолва имени по id (для чата без готового JOIN можно подписаться отдельно;
     * предпочтительно — {@link ChatRepository#observeMessagesWithSender(String)}).
     */
    @NonNull
    public LiveData<String> observeUserFullName(@NonNull String userId) {
        return db.userDao().observeFullName(userId);
    }

    public void updateCurrentProfile(
            @NonNull String fullName,
            @NonNull String position,
            @NonNull Gender gender,
            boolean militaryLiable
    ) {
        String id = sessionManager.getUserIdOrEmpty();
        if (id.isEmpty()) {
            return;
        }
        AppDatabase.dbExecutor.execute(() -> {
            UserEntity user = db.userDao().getUserSync(id);
            if (user == null) {
                return;
            }
            user.fullName = fullName;
            user.position = position;
            user.gender = gender;
            user.isMilitaryLiable = gender == Gender.MALE && militaryLiable;
            user.militaryDocument = user.isMilitaryLiable ? user.militaryDocument : null;
            user.updatedAt = System.currentTimeMillis();
            user.isSynced = false;
            db.userDao().upsert(user);
            firestoreUserSync.pushUser(user);
        });
    }
}
