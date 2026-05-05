package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
import com.ozhd.kadrov.employeesupport.data.local.pojo.RequestWithAuthor;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;

import java.util.List;

@Dao
public interface RequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(RequestEntity request);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RequestEntity> requests);

    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<RequestEntity>> observeForUser(String userId);

    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    LiveData<List<RequestEntity>> observeAll();

    @Query("SELECT requests.*, users.fullName AS authorFullName FROM requests "
            + "LEFT JOIN users ON users.id = requests.userId "
            + "ORDER BY requests.createdAt DESC")
    LiveData<List<RequestWithAuthor>> observeAllWithAuthor();

    @Query("SELECT * FROM requests WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<RequestEntity>> observeByStatus(RequestStatus status);

    @Query("UPDATE requests SET status = :status, rejectionReason = :rejectionReason, "
            + "updatedAt = :updatedAt, isSynced = 0 WHERE id = :requestId")
    void updateStatus(String requestId, RequestStatus status, String rejectionReason, long updatedAt);

    @Query("SELECT * FROM requests WHERE id = :id LIMIT 1")
    RequestEntity getByIdSync(String id);
}
