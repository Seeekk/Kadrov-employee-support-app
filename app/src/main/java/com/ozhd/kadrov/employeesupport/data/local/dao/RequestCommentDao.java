package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestCommentEntity;

import java.util.List;

@Dao
public interface RequestCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RequestCommentEntity comment);

    @Query("SELECT * FROM request_comments WHERE requestId = :requestId ORDER BY createdAt ASC")
    LiveData<List<RequestCommentEntity>> observeForRequest(String requestId);
}
