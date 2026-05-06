package com.ozhd.kadrov.employeesupport.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
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

    /** Хэш пароля или пусто, если авторизация выполняется только через backend API. */
    public String passwordHash;

    @NonNull
    public String fullName = "";

    public String phone;
    public String avatarUrl;
    public String departmentId;
    public String position;

    @NonNull
    public Gender gender = Gender.MALE;

    public String militaryDocument;

    @NonNull
    public UserRole role = UserRole.EMPLOYEE;

    @NonNull
    public ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    public boolean isMilitaryLiable;

    public boolean isActive = true;

    /** Причина увольнения/деактивации, если аккаунт отключён HR. */
    public String dismissalReason;

    /** Время последнего обновления записи на сервере (epoch millis). */
    public long updatedAt;

    /** Флаг успешной синхронизации с сервером. */
    public boolean isSynced;
}
