package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.pojo.RequestWithAuthor;
import com.ozhd.kadrov.employeesupport.data.repository.RequestWorkflowRepository;

import java.util.List;

public class RequestsViewModel extends AndroidViewModel {

    private final RequestWorkflowRepository repository;
    private final LiveData<List<RequestWithAuthor>> requests;
    private final boolean hr;

    public RequestsViewModel(@NonNull Application application) {
        super(application);
        repository = new RequestWorkflowRepository(application);
        requests = repository.observeAllRequests();
        hr = RoleHelper.isHr(new SessionManager(application).getRole());
    }

    public LiveData<List<RequestWithAuthor>> getRequests() {
        return requests;
    }

    public boolean isHr() {
        return hr;
    }

    public void approve(@NonNull String requestId) {
        repository.approve(requestId);
    }

    public void reject(@NonNull String requestId, @NonNull String reason) {
        repository.reject(requestId, reason);
    }
}
