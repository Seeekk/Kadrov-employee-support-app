package com.ozhd.kadrov.employeesupport;

import android.app.Application;

import com.ozhd.kadrov.employeesupport.core.ThemeHelper;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder;
import com.ozhd.kadrov.employeesupport.data.remote.RetrofitModule;

/**
 * Точка входа приложения: запускает начальное наполнение Room (роли / демо-пользователи).
 */
public class EmployeeSupportApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.apply(this);
        RetrofitModule.init(this);
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.dbExecutor.execute(() -> DatabaseSeeder.seedIfNeeded(db));
    }
}
