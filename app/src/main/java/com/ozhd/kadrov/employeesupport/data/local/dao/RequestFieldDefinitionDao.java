package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;

import java.util.List;

@Dao
public interface RequestFieldDefinitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(RequestFieldDefinitionEntity field);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RequestFieldDefinitionEntity> fields);

    @Query("SELECT * FROM request_fields WHERE kindId = :kindId ORDER BY sortOrder ASC, label COLLATE NOCASE ASC")
    LiveData<List<RequestFieldDefinitionEntity>> observeForKind(String kindId);

    @Query("SELECT * FROM request_fields WHERE kindId = :kindId ORDER BY sortOrder ASC")
    List<RequestFieldDefinitionEntity> getForKindSync(String kindId);

    @Query("DELETE FROM request_fields WHERE kindId = :kindId")
    void deleteForKind(String kindId);

    @Query("DELETE FROM request_fields WHERE id = :id")
    void deleteById(String id);
}
