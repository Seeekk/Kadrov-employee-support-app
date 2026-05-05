package com.ozhd.kadrov.employeesupport.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.ui.chat.ChatFragment;
import com.ozhd.kadrov.employeesupport.ui.common.SectionPlaceholderFragment;
import com.ozhd.kadrov.employeesupport.ui.profile.ProfileFragment;
import com.ozhd.kadrov.employeesupport.ui.requests.RequestsFragment;

/**
 * Вкладки «Главная»: профиль, чат, (опционально) модерация HR, заявки.
 */
public class HomePagerAdapter extends FragmentStateAdapter {

    private final UserRole role;
    private final String titleModeration;

    public HomePagerAdapter(@NonNull Fragment fragment,
                            @NonNull UserRole role,
                            @NonNull String titleModeration) {
        super(fragment);
        this.role = role;
        this.titleModeration = titleModeration;
    }

    @Override
    public int getItemCount() {
        return RoleHelper.isHr(role) ? 4 : 3;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        boolean hr = RoleHelper.isHr(role);
        if (hr) {
            switch (position) {
                case 0:
                    return ProfileFragment.newInstance();
                case 1:
                    return ChatFragment.newInstance();
                case 2:
                    return SectionPlaceholderFragment.newInstance(titleModeration);
                case 3:
                default:
                    return RequestsFragment.newInstance();
            }
        } else {
            switch (position) {
                case 0:
                    return ProfileFragment.newInstance();
                case 1:
                    return ChatFragment.newInstance();
                case 2:
                default:
                    return RequestsFragment.newInstance();
            }
        }
    }
}
