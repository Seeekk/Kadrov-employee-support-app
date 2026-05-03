package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.data.repository.HrAdminRepository;

import java.util.List;

/**
 * MVVM: консоль HR — отделы и CRUD через {@link HrAdminRepository}.
 */
public class HrDepartmentsViewModel extends AndroidViewModel {

    private final HrAdminRepository repo;
    private final LiveData<List<DepartmentEntity>> departments;

    public HrDepartmentsViewModel(@NonNull Application application) {
        super(application);
        repo = new HrAdminRepository(application);
        departments = repo.observeDepartments();
    }

    @NonNull
    public LiveData<List<DepartmentEntity>> getDepartments() {
        return departments;
    }

    @NonNull
    public HrAdminRepository getRepository() {
        return repo;
    }
}
