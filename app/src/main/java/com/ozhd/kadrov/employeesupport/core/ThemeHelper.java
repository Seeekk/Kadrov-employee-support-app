package com.ozhd.kadrov.employeesupport.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Переключение светлой / тёмной темы и режима «как в системе» (Material3 DayNight).
 */
public final class ThemeHelper {

    private static final String PREFS = "kadrov_app_prefs";
    private static final String KEY_THEME = "theme_mode";

    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";
    public static final String MODE_FOLLOW_SYSTEM = "follow_system";

    private ThemeHelper() {
    }

    @NonNull
    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Применить сохранённый режим (вызывать из {@link android.app.Application#onCreate()}). */
    public static void apply(@NonNull Context context) {
        AppCompatDelegate.setDefaultNightMode(nightModeFromPrefs(context));
    }

    public static int nightModeFromPrefs(@NonNull Context context) {
        String mode = prefs(context).getString(KEY_THEME, MODE_FOLLOW_SYSTEM);
        if (MODE_LIGHT.equals(mode)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (MODE_DARK.equals(mode)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    @NonNull
    public static String getSavedMode(@NonNull Context context) {
        String v = prefs(context).getString(KEY_THEME, MODE_FOLLOW_SYSTEM);
        return v != null ? v : MODE_FOLLOW_SYSTEM;
    }

    public static void saveAndApply(@NonNull Context context, @NonNull String mode) {
        prefs(context).edit().putString(KEY_THEME, mode).apply();
        AppCompatDelegate.setDefaultNightMode(nightModeFromPrefs(context));
    }
}
