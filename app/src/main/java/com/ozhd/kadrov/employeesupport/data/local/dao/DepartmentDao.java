package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;

import java.util.List;

@Dao
public interface DepartmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<DepartmentEntity> departments);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DepartmentEntity department);

    @Query("SELECT * FROM departments ORDER BY name COLLATE NOCASE")
    LiveData<List<DepartmentEntity>> observeAll();

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    DepartmentEntity getById(String id);

    @Query("DELETE FROM departments WHERE id = :id")
    void deleteById(String id);
}
