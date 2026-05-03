package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.repository.UserRepository;

/**
 * Роль и профиль текущего пользователя для MainActivity и вложенных вкладок.
 */
public class SessionViewModel extends AndroidViewModel {

    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final MutableLiveData<UserRole> roleLiveData = new MutableLiveData<>();

    public SessionViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        userRepository = new UserRepository(application);
        roleLiveData.setValue(sessionManager.getRole());
    }

    public void refreshRoleFromSession() {
        roleLiveData.setValue(sessionManager.getRole());
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
}
