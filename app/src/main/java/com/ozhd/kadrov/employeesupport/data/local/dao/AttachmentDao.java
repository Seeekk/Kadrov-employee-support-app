package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.AttachmentEntity;

import java.util.List;

@Dao
public interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<AttachmentEntity> attachments);

    @Query("SELECT * FROM attachments WHERE requestId = :requestId")
    LiveData<List<AttachmentEntity>> observeForRequest(String requestId);
}
