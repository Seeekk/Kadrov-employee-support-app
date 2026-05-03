package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.repository.RequestCatalogRepository;

import java.util.List;

/**
 * Раздел «Создание заявки»: типы подтягиваются из Room (HR управляет ими в консоли).
 */
public class CreateRequestViewModel extends AndroidViewModel {

    private final RequestCatalogRepository catalogRepository;
    private final LiveData<List<RequestKindEntity>> kinds;

    public CreateRequestViewModel(@NonNull Application application) {
        super(application);
        catalogRepository = new RequestCatalogRepository(application);
        kinds = catalogRepository.observeActiveKinds();
    }

    public LiveData<List<RequestKindEntity>> getKinds() {
        return kinds;
    }
}
