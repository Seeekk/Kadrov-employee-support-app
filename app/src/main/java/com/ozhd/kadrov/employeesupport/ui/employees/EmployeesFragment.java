package com.ozhd.kadrov.employeesupport.ui.employees;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.databinding.FragmentSectionTabsBinding;
import com.ozhd.kadrov.employeesupport.ui.common.SimpleTabPagerAdapter;
import com.ozhd.kadrov.employeesupport.ui.common.ThemeSpinnerHelper;

/**
 * Раздел «Сотрудники»: подвкладки по категориям списков; переключение темы — как на «Главной».
 */
public class EmployeesFragment extends Fragment {

    public static EmployeesFragment newInstance() {
        return new EmployeesFragment();
    }

    private FragmentSectionTabsBinding binding;
    private TabLayoutMediator mediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSectionTabsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.themeRow.setVisibility(View.VISIBLE);
        ThemeSpinnerHelper.attach(binding.spinnerTheme, this);

        String[] titles = new String[]{
                getString(R.string.emp_tab_all),
                getString(R.string.emp_tab_dept),
                getString(R.string.emp_tab_military),
                getString(R.string.emp_tab_dismissed)
        };
        binding.viewPager.setAdapter(new SimpleTabPagerAdapter(this, titles));
        binding.viewPager.setOffscreenPageLimit(titles.length);
        mediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position]));
        mediator.attach();
    }

    @Override
    public void onDestroyView() {
        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
