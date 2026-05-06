package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.model.NotificationType;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<NotificationEntity> items);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> observeUnread(String userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    LiveData<Integer> observeUnreadCount(String userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> observeAllForUser(String userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> observeByType(String userId, NotificationType type);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<NotificationEntity> getAllForUserSync(String userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markRead(String id);
}
