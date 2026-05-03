package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Подразделение организации.
 */
@Entity(tableName = "departments")
public class DepartmentEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String name = "";

    public String description;
}
