package com.ozhd.kadrov.employeesupport.ui.common;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ViewPager2: по одному {@link SectionPlaceholderFragment} на каждую подпись вкладки.
 */
public class SimpleTabPagerAdapter extends FragmentStateAdapter {

    private final String[] titles;

    public SimpleTabPagerAdapter(@NonNull Fragment fragment, @NonNull String[] titles) {
        super(fragment);
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return SectionPlaceholderFragment.newInstance(titles[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
