package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreNotificationSync;

import java.util.List;

public class NotificationRepository {

    private final AppDatabase db;
    private final SessionManager session;
    private final FirestoreNotificationSync firestoreSync;

    public NotificationRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
        this.session = new SessionManager(context);
        this.firestoreSync = new FirestoreNotificationSync(db);
    }

    public LiveData<List<NotificationEntity>> observeMine() {
        return db.notificationDao().observeAllForUser(session.getUserIdOrEmpty());
    }

    public LiveData<Integer> observeUnreadCount() {
        return db.notificationDao().observeUnreadCount(session.getUserIdOrEmpty());
    }

    public ListenerRegistration attachRealtime() {
        return firestoreSync.listen(session.getUserIdOrEmpty());
    }

    public void markRead(@NonNull String notificationId) {
        AppDatabase.dbExecutor.execute(() -> db.notificationDao().markRead(notificationId));
    }
}
