package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<UserEntity> users);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<UserEntity> observeUser(String id);

    /**
     * Пример получения ФИО по id пользователя (аналог резолва имени для {@code senderId} в чате;
     * в списке сообщений удобнее JOIN — см. {@link com.ozhd.kadrov.employeesupport.data.local.dao.ChatDao}).
     */
    @Query("SELECT fullName FROM users WHERE id = :id LIMIT 1")
    LiveData<String> observeFullName(String id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getUserSync(String id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);

    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY fullName COLLATE NOCASE")
    LiveData<List<UserEntity>> observeActiveUsers();

    /** Для консоли HR: все активные учётные записи (при необходимости расширьте фильтр). */
    @Query("SELECT * FROM users ORDER BY fullName COLLATE NOCASE")
    LiveData<List<UserEntity>> observeAllUsers();

    @Query("SELECT * FROM users WHERE departmentId = :deptId AND isActive = 1 ORDER BY fullName COLLATE NOCASE")
    LiveData<List<UserEntity>> observeUsersByDepartment(String deptId);

    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1")
    LiveData<List<UserEntity>> observeByRole(UserRole role);

    @Query("SELECT COUNT(*) FROM users")
    int countUsers();

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(String id);
}
