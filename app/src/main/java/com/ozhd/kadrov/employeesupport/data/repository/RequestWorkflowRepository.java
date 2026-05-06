package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.pojo.RequestWithAuthor;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreRequestSync;

import java.util.List;

/**
 * Список заявок для сотрудников и модерация HR.
 */
public class RequestWorkflowRepository {

    private final AppDatabase db;
    private final FirestoreRequestSync firestoreSync;

    public RequestWorkflowRepository(@NonNull Context context) {
        db = AppDatabase.getInstance(context);
        firestoreSync = new FirestoreRequestSync(db);
        firestoreSync.listenAll();
    }

    public LiveData<List<RequestWithAuthor>> observeAllRequests() {
        return db.requestDao().observeAllWithAuthor();
    }

    public void approve(@NonNull String requestId) {
        AppDatabase.dbExecutor.execute(() ->
                db.requestDao().updateStatus(requestId, RequestStatus.APPROVED, null, System.currentTimeMillis()));
    }

    public void reject(@NonNull String requestId, @NonNull String reason) {
        AppDatabase.dbExecutor.execute(() ->
                db.requestDao().updateStatus(requestId, RequestStatus.REJECTED, reason, System.currentTimeMillis()));
    }
}
