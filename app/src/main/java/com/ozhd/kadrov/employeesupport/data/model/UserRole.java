package com.ozhd.kadrov.employeesupport.data.model;

/**
 * Роли пользователей: определяются сервером и кэшируются в Room.
 */
public enum UserRole {
    EMPLOYEE,
    HR;

    public static UserRole fromString(String value) {
        if (value == null) return EMPLOYEE;
        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EMPLOYEE;
        }
    }
}
