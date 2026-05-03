package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.repository.HrAdminRepository;

import java.util.List;

/**
 * MVVM: консоль HR — список сотрудников и CRUD через {@link HrAdminRepository}.
 */
public class HrEmployeesViewModel extends AndroidViewModel {

    private final HrAdminRepository repo;
    private final LiveData<List<UserEntity>> users;

    public HrEmployeesViewModel(@NonNull Application application) {
        super(application);
        repo = new HrAdminRepository(application);
        users = repo.observeAllUsers();
    }

    @NonNull
    public LiveData<List<UserEntity>> getUsers() {
        return users;
    }

    @NonNull
    public HrAdminRepository getRepository() {
        return repo;
    }
}
