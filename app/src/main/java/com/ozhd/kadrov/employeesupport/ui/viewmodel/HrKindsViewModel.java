package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.repository.RequestCatalogRepository;

import java.util.Collections;
import java.util.List;

/**
 * Выбранный тип заявки в консоли HR → подгрузка полей через {@link Transformations#switchMap}.
 */
public class HrKindsViewModel extends AndroidViewModel {

    private final RequestCatalogRepository catalog;
    private final MutableLiveData<String> selectedKindId = new MutableLiveData<>();

    public HrKindsViewModel(@NonNull Application application) {
        super(application);
        catalog = new RequestCatalogRepository(application);
    }

    public LiveData<List<RequestKindEntity>> getKinds() {
        return catalog.observeAllKinds();
    }

    public void selectKind(@NonNull String kindId) {
        selectedKindId.setValue(kindId);
    }

    public LiveData<List<RequestFieldDefinitionEntity>> observeFieldsForSelection() {
        return Transformations.switchMap(selectedKindId, id -> {
            if (id == null) {
                MutableLiveData<List<RequestFieldDefinitionEntity>> empty = new MutableLiveData<>();
                empty.setValue(Collections.emptyList());
                return empty;
            }
            return catalog.observeFieldsForKind(id);
        });
    }
}
