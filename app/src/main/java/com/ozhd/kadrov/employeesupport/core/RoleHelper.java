package com.ozhd.kadrov.employeesupport.core;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel;

/**
 * Утилиты проверки роли: {@link UserRole#HR} против {@link UserRole#EMPLOYEE}.
 * <p>
 * Скрытие пунктов меню — в {@link com.ozhd.kadrov.employeesupport.ui.main.MainActivity}
 * по {@link com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel#getRole()};
 * вход на экраны HR — через {@link #ensureHrOrToast(Fragment)}.
 */
public final class RoleHelper {

    private RoleHelper() {
    }

    /** Проверка роли HR (администратор справочников и типов заявок). */
    public static boolean isHr(@NonNull UserRole role) {
        return role == UserRole.HR;
    }

    /**
     * Ограничение доступа к функциям HR: при роли сотрудника показываем Toast и возвращаем false.
     */
    public static boolean ensureHrOrToast(@NonNull Fragment fragment) {
        SessionViewModel vm = new ViewModelProvider(fragment.requireActivity())
                .get(SessionViewModel.class);
        if (!isHr(vm.getRoleValue())) {
            Toast.makeText(fragment.requireContext(), R.string.hr_only_message, Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        return true;
    }

    public static boolean employeeMaySeeEmployeesTab(@NonNull UserRole role) {
        return true;
    }
}
