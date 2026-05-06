package com.ozhd.kadrov.employeesupport.data.model;

public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static ApprovalStatus fromString(String value) {
        if (value == null) return PENDING;
        try {
            return ApprovalStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
