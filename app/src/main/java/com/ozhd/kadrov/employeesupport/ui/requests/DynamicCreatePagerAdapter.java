package com.ozhd.kadrov.employeesupport.ui.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Подвкладки «Создание заявки» строятся из списка {@link RequestKindEntity} из БД.
 */
public class DynamicCreatePagerAdapter extends FragmentStateAdapter {

    private final List<RequestKindEntity> kinds;

    public DynamicCreatePagerAdapter(@NonNull Fragment fragment,
                                     @Nullable List<RequestKindEntity> kinds) {
        super(fragment);
        this.kinds = kinds != null ? kinds : new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        RequestKindEntity k = kinds.get(position);
        return DynamicRequestFormFragment.newInstance(k.id, k.name);
    }

    @Override
    public int getItemCount() {
        return kinds.size();
    }
}
