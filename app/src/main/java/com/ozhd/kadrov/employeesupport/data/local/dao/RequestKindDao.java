package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;

import java.util.List;

@Dao
public interface RequestKindDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(RequestKindEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RequestKindEntity> list);

    @Query("SELECT * FROM request_kinds WHERE isActive = 1 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    LiveData<List<RequestKindEntity>> observeActiveKinds();

    @Query("SELECT * FROM request_kinds ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    LiveData<List<RequestKindEntity>> observeAllKinds();

    @Query("SELECT * FROM request_kinds WHERE id = :id LIMIT 1")
    LiveData<RequestKindEntity> observeKind(String id);

    @Query("SELECT * FROM request_kinds WHERE id = :id LIMIT 1")
    RequestKindEntity getByIdSync(String id);

    @Query("DELETE FROM request_kinds WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM request_kinds")
    int countKinds();

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM request_kinds")
    int maxSortOrder();
}
