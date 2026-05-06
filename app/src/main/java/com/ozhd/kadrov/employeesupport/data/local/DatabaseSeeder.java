package com.ozhd.kadrov.employeesupport.data.local;

import androidx.annotation.NonNull;

import com.ozhd.kadrov.employeesupport.core.DemoAccounts;
import com.ozhd.kadrov.employeesupport.core.FieldSchemaJson;
import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.FieldInputType;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Начальное наполнение БД: пользователи, подразделение, шаблоны типов заявок и полей.
 */
public final class DatabaseSeeder {

    public static final String KIND_VACATION = "rk_vacation";
    public static final String KIND_BONUS = "rk_bonus";
    public static final String KIND_TRANSFER = "rk_transfer";
    public static final String KIND_OTHER = "rk_other";

    private DatabaseSeeder() {
    }

    public static void seedIfNeeded(@NonNull AppDatabase db) {
        seedUsersIfNeeded(db);
        seedRequestKindsIfNeeded(db);
        seedRequestsIfNeeded(db);
    }

    private static void seedUsersIfNeeded(AppDatabase db) {
        if (db.userDao().countUsers() >= 2) {
            return;
        }
        DepartmentEntity dept = new DepartmentEntity();
        dept.id = DemoAccounts.SEED_DEPARTMENT_ID;
        dept.name = "Кадровый центр ОЖД";
        dept.description = "Подразделение по умолчанию";

        UserEntity employee = new UserEntity();
        employee.id = DemoAccounts.SEED_USER_EMPLOYEE_ID;
        employee.email = DemoAccounts.EMPLOYEE_EMAIL;
        employee.passwordHash = null;
        employee.fullName = "Иванов Иван (сотрудник)";
        employee.phone = "+7 900 000-00-01";
        employee.avatarUrl = null;
        employee.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        employee.position = "Специалист";
        employee.gender = Gender.MALE;
        employee.militaryDocument = "MB-000001";
        employee.role = UserRole.EMPLOYEE;
        employee.approvalStatus = ApprovalStatus.APPROVED;
        employee.isMilitaryLiable = true;
        employee.isActive = true;
        employee.updatedAt = System.currentTimeMillis();
        employee.isSynced = true;

        UserEntity hr = new UserEntity();
        hr.id = DemoAccounts.SEED_USER_HR_ID;
        hr.email = DemoAccounts.HR_EMAIL;
        hr.passwordHash = null;
        hr.fullName = "Петрова Мария (HR)";
        hr.phone = "+7 900 000-00-02";
        hr.avatarUrl = null;
        hr.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        hr.position = "HR менеджер";
        hr.gender = Gender.FEMALE;
        hr.militaryDocument = null;
        hr.role = UserRole.HR;
        hr.approvalStatus = ApprovalStatus.APPROVED;
        hr.isMilitaryLiable = false;
        hr.isActive = true;
        hr.updatedAt = System.currentTimeMillis();
        hr.isSynced = true;

        db.departmentDao().upsert(dept);
        db.userDao().upsertAll(Arrays.asList(employee, hr));
    }

    private static void seedRequestKindsIfNeeded(AppDatabase db) {
        if (db.requestKindDao().countKinds() > 0) {
            return;
        }
        long now = System.currentTimeMillis();
        List<RequestKindEntity> kinds = new ArrayList<>();
        kinds.add(kind(KIND_VACATION, "Отпуск / больничный",
                "Оформление отпуска или больничного", 10, FieldSchemaJson.EXAMPLE_VACATION, now));
        kinds.add(kind(KIND_BONUS, "Премия / повышение", "Инициация премии или повышения", 20,
                FieldSchemaJson.EXAMPLE_BONUS, now));
        kinds.add(kind(KIND_TRANSFER, "Перевод / обучение", "Перевод в другой отдел или обучение", 30,
                FieldSchemaJson.EXAMPLE_TRANSFER, now));
        kinds.add(kind(KIND_OTHER, "Другое", "Произвольная заявка", 40,
                FieldSchemaJson.EXAMPLE_OTHER, now));
        db.requestKindDao().upsertAll(kinds);

        List<RequestFieldDefinitionEntity> fields = new ArrayList<>();
        fields.add(field("f_v_1", KIND_VACATION, "startDate", "Дата начала", FieldInputType.DATE, true, 1));
        fields.add(field("f_v_2", KIND_VACATION, "endDate", "Дата окончания", FieldInputType.DATE, true, 2));
        fields.add(field("f_v_3", KIND_VACATION, "reason", "Комментарий", FieldInputType.LONG_TEXT, false, 3));
        fields.add(field("f_b_1", KIND_BONUS, "amount", "Сумма (руб.)", FieldInputType.NUMBER, true, 1));
        fields.add(field("f_b_2", KIND_BONUS, "justification", "Обоснование", FieldInputType.LONG_TEXT, true, 2));
        fields.add(field("f_t_1", KIND_TRANSFER, "targetDept", "Целевое подразделение", FieldInputType.TEXT, true, 1));
        fields.add(field("f_t_2", KIND_TRANSFER, "course", "Название курса / программы", FieldInputType.TEXT, false, 2));
        fields.add(field("f_o_1", KIND_OTHER, "subject", "Тема", FieldInputType.TEXT, true, 1));
        fields.add(field("f_o_2", KIND_OTHER, "details", "Подробности", FieldInputType.LONG_TEXT, false, 2));
        db.requestFieldDao().upsertAll(fields);
    }

    private static RequestKindEntity kind(String id, String name, String description, int sort,
                                         String schemaJson, long now) {
        RequestKindEntity k = new RequestKindEntity();
        k.id = id;
        k.name = name;
        k.description = description;
        k.sortOrder = sort;
        k.isActive = true;
        k.fieldSchemaJson = schemaJson;
        k.updatedAt = now;
        k.isSynced = true;
        return k;
    }

    private static RequestFieldDefinitionEntity field(String id, String kindId, String key, String label,
                                                      FieldInputType type, boolean required, int order) {
        RequestFieldDefinitionEntity f = new RequestFieldDefinitionEntity();
        f.id = id;
        f.kindId = kindId;
        f.fieldKey = key;
        f.label = label;
        f.inputType = type;
        f.required = required;
        f.sortOrder = order;
        f.validationJson = null;
        return f;
    }

    private static void seedRequestsIfNeeded(@NonNull AppDatabase db) {
        if (db.requestDao().countRequestsSync() > 0) {
            return;
        }
        long now = System.currentTimeMillis();
        RequestEntity request = new RequestEntity();
        request.id = "rq-seed-1";
        request.userId = DemoAccounts.SEED_USER_EMPLOYEE_ID;
        request.kindId = KIND_VACATION;
        request.status = RequestStatus.PENDING;
        request.title = "Отпуск / больничный";
        request.description = "Тестовая заявка для проверки отображения списка";
        request.payloadJson = "{\"startDate\":\"2026-05-10\",\"endDate\":\"2026-05-15\",\"reason\":\"Тест\"}";
        request.rejectionReason = null;
        request.createdAt = now;
        request.updatedAt = now;
        request.isSynced = true;
        db.requestDao().upsert(request);
    }

    /** Генерация id для полей/типов, создаваемых HR в UI. */
    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
