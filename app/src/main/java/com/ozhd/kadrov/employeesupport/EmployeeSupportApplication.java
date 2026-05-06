package com.ozhd.kadrov.employeesupport;

import android.app.Application;

import com.ozhd.kadrov.employeesupport.core.ThemeHelper;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.DatabaseSeeder;
import com.ozhd.kadrov.employeesupport.data.remote.RetrofitModule;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseAuthHelper;

import android.util.Log;

/**
 * Точка входа приложения: запускает начальное наполнение Room (роли / демо-пользователи).
 */
public class EmployeeSupportApplication extends Application {

    private static final String TAG = "EmployeeSupportApp";

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.apply(this);
        RetrofitModule.init(this);
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.dbExecutor.execute(() -> {
            try {
                DatabaseSeeder.seedIfNeeded(db);
            } catch (Throwable t) {
                Log.e(TAG, "DatabaseSeeder failed", t);
            }
        });
        try {
            FirebaseAuthHelper.syncRoleFromCustomClaims(new SessionManager(this));
        } catch (Throwable t) {
            Log.e(TAG, "syncRoleFromCustomClaims failed", t);
        }
    }
}
