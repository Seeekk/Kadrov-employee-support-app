package com.ozhd.kadrov.employeesupport.data.model;

public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static RequestStatus fromString(String value) {
        if (value == null) return PENDING;
        try {
            return RequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
