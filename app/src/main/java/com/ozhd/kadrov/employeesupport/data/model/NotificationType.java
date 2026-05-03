package com.ozhd.kadrov.employeesupport.data.model;

public enum NotificationType {
    SYSTEM,
    CHAT,
    REQUEST;

    public static NotificationType fromString(String value) {
        if (value == null) return SYSTEM;
        try {
            return NotificationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SYSTEM;
        }
    }
}
