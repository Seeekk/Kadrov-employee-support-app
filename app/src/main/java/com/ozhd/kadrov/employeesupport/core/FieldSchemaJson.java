package com.ozhd.kadrov.employeesupport.core;

/**
 * Пример JSON-схемы динамической формы для синхронизации с сервером и резервного хранения в
 * {@link com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity#fieldSchemaJson}.
 * <p>
 * Сервер и клиент договариваются о версии схемы; парсер формы может использовать либо эту JSON,
 * либо нормализованные строки {@link com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity}.
 */
public final class FieldSchemaJson {

    /**
     * Пример: отпуск — даты + комментарий.
     */
    public static final String EXAMPLE_VACATION = "{"
            + "\"version\":1,"
            + "\"title\":\"Отпуск / больничный\","
            + "\"fields\":["
            + "{\"key\":\"startDate\",\"label\":\"Дата начала\",\"type\":\"DATE\",\"required\":true},"
            + "{\"key\":\"endDate\",\"label\":\"Дата окончания\",\"type\":\"DATE\",\"required\":true},"
            + "{\"key\":\"reason\",\"label\":\"Причина\",\"type\":\"LONG_TEXT\",\"required\":false}"
            + "]}";

    /**
     * Пример: премия — сумма и обоснование.
     */
    public static final String EXAMPLE_BONUS = "{"
            + "\"version\":1,"
            + "\"fields\":["
            + "{\"key\":\"amount\",\"label\":\"Сумма (руб.)\",\"type\":\"NUMBER\",\"required\":true},"
            + "{\"key\":\"justification\",\"label\":\"Обоснование\",\"type\":\"LONG_TEXT\",\"required\":true}"
            + "]}";

    /**
     * Пример: перевод / обучение — целевое подразделение и курс.
     */
    public static final String EXAMPLE_TRANSFER = "{"
            + "\"version\":1,"
            + "\"title\":\"Перевод / обучение\","
            + "\"fields\":["
            + "{\"key\":\"targetDept\",\"label\":\"Целевое подразделение\",\"type\":\"TEXT\",\"required\":true,\"sortOrder\":1},"
            + "{\"key\":\"course\",\"label\":\"Название курса / программы\",\"type\":\"TEXT\",\"required\":false,\"sortOrder\":2}"
            + "]}";

    /**
     * Пример: произвольная заявка — тема и подробности.
     */
    public static final String EXAMPLE_OTHER = "{"
            + "\"version\":1,"
            + "\"title\":\"Другое\","
            + "\"fields\":["
            + "{\"key\":\"subject\",\"label\":\"Тема\",\"type\":\"TEXT\",\"required\":true,\"sortOrder\":1},"
            + "{\"key\":\"details\",\"label\":\"Подробности\",\"type\":\"LONG_TEXT\",\"required\":false,\"sortOrder\":2}"
            + "]}";

    private FieldSchemaJson() {
    }
}
