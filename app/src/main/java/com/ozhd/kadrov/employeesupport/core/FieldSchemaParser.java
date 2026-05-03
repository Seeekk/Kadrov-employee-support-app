package com.ozhd.kadrov.employeesupport.core;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Парсинг JSON-схемы полей без пересборки приложения: HR или сервер меняют
 * {@link com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity#fieldSchemaJson},
 * клиент строит список полей для {@link com.ozhd.kadrov.employeesupport.ui.requests.DynamicFormInflater}.
 * <p>
 * Если в Room уже есть строки в {@code request_fields}, они имеют приоритет; этот парсер — запасной
 * путь, когда поля пришли только в JSON (например, после миграции с другого бэкенда).
 */
public final class FieldSchemaParser {

    private static final Gson GSON = new Gson();

    private FieldSchemaParser() {
    }

    /**
     * @param kindId идентификатор типа заявки (для синтетических id полей)
     * @param json    объект с массивом {@code fields}
     */
    @NonNull
    public static List<RequestFieldDefinitionEntity> parseFieldsFromSchema(
            @NonNull String kindId,
            @NonNull String json) {
        try {
            SchemaRoot root = GSON.fromJson(json, SchemaRoot.class);
            if (root == null || root.fields == null || root.fields.isEmpty()) {
                return Collections.emptyList();
            }
            List<RequestFieldDefinitionEntity> out = new ArrayList<>();
            int index = 0;
            for (SchemaField f : root.fields) {
                if (f == null || f.key == null || f.label == null) {
                    continue;
                }
                RequestFieldDefinitionEntity e = new RequestFieldDefinitionEntity();
                e.id = "__json__" + kindId + "_" + f.key;
                e.kindId = kindId;
                e.fieldKey = f.key;
                e.label = f.label;
                e.inputType = mapType(f.type);
                e.required = f.required;
                e.sortOrder = f.sortOrder != null ? f.sortOrder : index;
                e.validationJson = f.validationJson;
                out.add(e);
                index++;
            }
            return out;
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    private static FieldInputType mapType(String type) {
        if (type == null) {
            return FieldInputType.TEXT;
        }
        return FieldInputType.fromString(type);
    }

    private static final class SchemaRoot {
        @SerializedName("fields")
        List<SchemaField> fields;
    }

    private static final class SchemaField {
        @SerializedName("key")
        String key;
        @SerializedName("label")
        String label;
        /** DATE, NUMBER, LONG_TEXT, TEXT, FILE */
        @SerializedName("type")
        String type;
        @SerializedName("required")
        boolean required;
        @SerializedName("sortOrder")
        Integer sortOrder;
        @SerializedName("validationJson")
        String validationJson;
    }
}
