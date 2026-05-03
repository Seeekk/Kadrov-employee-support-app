package com.ozhd.kadrov.employeesupport.ui.common;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.ThemeHelper;

/**
 * Выпадающий список темы (светлая / как в системе / тёмная) для панели вкладок «Главная».
 */
public final class ThemeSpinnerHelper {

    private ThemeSpinnerHelper() {
    }

    public static void attach(@NonNull Spinner spinner, @NonNull Fragment fragment) {
        android.content.Context ctx = fragment.requireContext();
        String[] labels = new String[]{
                ctx.getString(R.string.theme_light),
                ctx.getString(R.string.theme_system),
                ctx.getString(R.string.theme_dark)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ctx,
                R.layout.item_theme_spinner,
                labels);
        adapter.setDropDownViewResource(R.layout.item_theme_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(positionForMode(ThemeHelper.getSavedMode(ctx)), false);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newMode = modeForPosition(position);
                if (newMode.equals(ThemeHelper.getSavedMode(fragment.requireContext()))) {
                    return;
                }
                ThemeHelper.saveAndApply(fragment.requireContext(), newMode);
                fragment.requireActivity().recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // не используется
            }
        });
    }

    private static int positionForMode(@NonNull String mode) {
        if (ThemeHelper.MODE_LIGHT.equals(mode)) {
            return 0;
        }
        if (ThemeHelper.MODE_DARK.equals(mode)) {
            return 2;
        }
        return 1;
    }

    @NonNull
    private static String modeForPosition(int position) {
        if (position == 0) {
            return ThemeHelper.MODE_LIGHT;
        }
        if (position == 2) {
            return ThemeHelper.MODE_DARK;
        }
        return ThemeHelper.MODE_FOLLOW_SYSTEM;
    }
}
