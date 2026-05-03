package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
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

    @Query("SELECT * FROM requests WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<RequestEntity>> observeByStatus(RequestStatus status);

    @Query("SELECT * FROM requests WHERE id = :id LIMIT 1")
    RequestEntity getByIdSync(String id);
}
