package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseInitHelper;
import com.ozhd.kadrov.employeesupport.data.repository.UserRepository;

/**
 * Роль и профиль текущего пользователя для MainActivity и вложенных вкладок.
 */
public class SessionViewModel extends AndroidViewModel {

    private static final String TAG = "SessionViewModel";

    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final MutableLiveData<UserRole> roleLiveData = new MutableLiveData<>();

    public SessionViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        userRepository = new UserRepository(application);
        roleLiveData.setValue(sessionManager.getRole());
        syncRoleFromFirestoreProfile();
    }

    public void refreshRoleFromSession() {
        roleLiveData.setValue(sessionManager.getRole());
        syncRoleFromFirestoreProfile();
    }

    public LiveData<UserRole> getRole() {
        return roleLiveData;
    }

    public LiveData<UserEntity> getCurrentUser() {
        return userRepository.observeCurrentUser();
    }

    @NonNull
    public UserRole getRoleValue() {
        UserRole r = roleLiveData.getValue();
        return r != null ? r : sessionManager.getRole();
    }

    /**
     * Роль в UI хранится в SessionManager. Для Firebase-сценария подтягиваем её
     * из users/{firebaseUid}.role, чтобы изменения в Firestore применялись без бэкенда.
     */
    private void syncRoleFromFirestoreProfile() {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return;
        }
        FirebaseUser firebaseUser;
        try {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            return;
        }
        if (firebaseUser == null) {
            return;
        }
        String firebaseUid = firebaseUser.getUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            return;
        }
        try {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(firebaseUid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        try {
                            if (snapshot == null || !snapshot.exists()) {
                                return;
                            }
                            String roleRaw = snapshot.getString("role");
                            if (roleRaw == null || roleRaw.trim().isEmpty()) {
                                return;
                            }
                            UserRole role = UserRole.fromString(roleRaw);
                            UserRole sessionRole = sessionManager.getRole();
                            // Не перезаписываем HR из устаревшего документа Firestore (часто role=EMPLOYEE для того же uid).
                            if (sessionRole == UserRole.HR && role == UserRole.EMPLOYEE) {
                                return;
                            }
                            String token = sessionManager.getAccessToken();
                            String userId = sessionManager.getUserIdOrEmpty();
                            if (token == null || userId.isEmpty()) {
                                return;
                            }
                            String email = sessionManager.getEmailOrEmpty();
                            sessionManager.saveSession(token, userId, role, email.isEmpty() ? null : email);
                            roleLiveData.postValue(role);
                        } catch (Throwable t) {
                            Log.e(TAG, "apply Firestore role snapshot", t);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "load users/{uid} for role", e));
        } catch (Throwable t) {
            Log.e(TAG, "syncRoleFromFirestoreProfile", t);
        }
    }
}
