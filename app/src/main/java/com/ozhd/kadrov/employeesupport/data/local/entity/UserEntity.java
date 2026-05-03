package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ozhd.kadrov.employeesupport.data.model.UserRole;

/**
 * Пользователь (кэш с сервера + локальные поля синхронизации).
 */
@Entity(
        tableName = "users",
        foreignKeys = @ForeignKey(
                entity = DepartmentEntity.class,
                parentColumns = "id",
                childColumns = "departmentId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("departmentId"), @Index("email")}
)
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String email = "";

    /** Хэш пароля или пусто, если авторизация только через Firebase. */
    public String passwordHash;

    @NonNull
    public String fullName = "";

    public String phone;
    public String avatarUrl;
    public String departmentId;

    @NonNull
    public UserRole role = UserRole.EMPLOYEE;

    public boolean isActive = true;

    /** Время последнего обновления записи на сервере (epoch millis). */
    public long updatedAt;

    /** Флаг успешной синхронизации с сервером. */
    public boolean isSynced;
}
