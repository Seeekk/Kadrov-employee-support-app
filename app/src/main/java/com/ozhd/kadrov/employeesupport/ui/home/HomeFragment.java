package com.ozhd.kadrov.employeesupport.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.databinding.FragmentSectionTabsBinding;
import com.ozhd.kadrov.employeesupport.ui.common.ThemeSpinnerHelper;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Раздел «Главная»: выпадающий список темы, TabLayout + ViewPager2; набор вкладок зависит от роли (модерация — только HR).
 */
public class HomeFragment extends Fragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
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

        SessionViewModel session = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        UserRole role = session.getRoleValue();
        setupPager(role);
    }

    private void setupPager(@NonNull UserRole role) {
        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.home_tab_profile));
        titles.add(getString(R.string.home_tab_chat));
        if (RoleHelper.isHr(role)) {
            titles.add(getString(R.string.home_tab_moderation));
        }
        titles.add(getString(R.string.home_tab_requests));

        HomePagerAdapter adapter = new HomePagerAdapter(
                this,
                role,
                getString(R.string.home_tab_moderation),
                getString(R.string.home_tab_requests)
        );
        binding.viewPager.setAdapter(adapter);
        // Не держим все вкладки в памяти сразу — иначе рано создаётся чат и тяжёлые сервисы.
        binding.viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = binding.tabLayout;
        mediator = new TabLayoutMediator(tabLayout, binding.viewPager, true, false,
                (tab, position) -> tab.setText(titles.get(position)));
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
