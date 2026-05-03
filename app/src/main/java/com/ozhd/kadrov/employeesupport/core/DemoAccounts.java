package com.ozhd.kadrov.employeesupport.core;

/**
 * Учётные записи для разработки и начальные id пользователей в Room ({@link com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder}).
 */
public final class DemoAccounts {

    public static final String SEED_DEPARTMENT_ID = "dept_ozhd_main";

    public static final String SEED_USER_EMPLOYEE_ID = "user_seed_employee";
    public static final String SEED_USER_HR_ID = "user_seed_hr";

    public static final String EMPLOYEE_EMAIL = "employee@ozhd.local";
    public static final String EMPLOYEE_PASSWORD = "employee123";

    public static final String HR_EMAIL = "hr@ozhd.local";
    public static final String HR_PASSWORD = "hr123";

    private DemoAccounts() {
    }

    public static boolean isEmployeeLogin(String email, String password) {
        return EMPLOYEE_EMAIL.equalsIgnoreCase(email.trim())
                && EMPLOYEE_PASSWORD.equals(password);
    }

    public static boolean isHrLogin(String email, String password) {
        return HR_EMAIL.equalsIgnoreCase(email.trim())
                && HR_PASSWORD.equals(password);
    }
}
