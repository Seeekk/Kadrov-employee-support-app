package com.ozhd.kadrov.employeesupport.data.local;

import androidx.room.TypeConverter;

import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;
import com.ozhd.kadrov.employeesupport.data.model.NotificationType;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

/**
 * Конвертеры примитивов для Room (роли и перечисления храним строками).
 */
public final class Converters {

    private Converters() {
    }

    @TypeConverter
    public static UserRole fromUserRole(String value) {
        return UserRole.fromString(value);
    }

    @TypeConverter
    public static String userRoleToString(UserRole role) {
        return role == null ? UserRole.EMPLOYEE.name() : role.name();
    }

    @TypeConverter
    public static FieldInputType fromFieldInputType(String value) {
        return FieldInputType.fromString(value);
    }

    @TypeConverter
    public static String fieldInputTypeToString(FieldInputType type) {
        return type == null ? FieldInputType.TEXT.name() : type.name();
    }

    @TypeConverter
    public static RequestStatus fromRequestStatus(String value) {
        return RequestStatus.fromString(value);
    }

    @TypeConverter
    public static String requestStatusToString(RequestStatus status) {
        return status == null ? RequestStatus.PENDING.name() : status.name();
    }

    @TypeConverter
    public static NotificationType fromNotificationType(String value) {
        return NotificationType.fromString(value);
    }

    @TypeConverter
    public static String notificationTypeToString(NotificationType type) {
        return type == null ? NotificationType.SYSTEM.name() : type.name();
    }
}
