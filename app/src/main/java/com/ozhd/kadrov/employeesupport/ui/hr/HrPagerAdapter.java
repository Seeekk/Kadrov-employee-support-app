package com.ozhd.kadrov.employeesupport.ui.hr;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Вкладки консоли HR: отделы, сотрудники, типы заявок и поля.
 */
public class HrPagerAdapter extends FragmentStateAdapter {

    public HrPagerAdapter(@NonNull HrConsoleFragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return HrDepartmentsFragment.newInstance();
            case 1:
                return HrEmployeesFragment.newInstance();
            case 2:
                return HrRegistrationsFragment.newInstance();
            default:
                return HrRequestKindsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
