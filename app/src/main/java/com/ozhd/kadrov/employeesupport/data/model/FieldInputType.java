package com.ozhd.kadrov.employeesupport.data.model;

/**
 * Тип поля динамической формы заявки (хранится в Room как строка).
 */
public enum FieldInputType {
    TEXT,
    LONG_TEXT,
    DATE,
    NUMBER,
    FILE;

    public static FieldInputType fromString(String value) {
        if (value == null) return TEXT;
        try {
            return FieldInputType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TEXT;
        }
    }
}
