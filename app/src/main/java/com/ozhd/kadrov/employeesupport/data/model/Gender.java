package com.ozhd.kadrov.employeesupport.data.model;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender fromString(String value) {
        if (value == null) return MALE;
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MALE;
        }
    }
}
