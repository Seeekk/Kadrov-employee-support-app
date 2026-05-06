package com.ozhd.kadrov.employeesupport.ui.hr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.databinding.FragmentHrConsoleBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.HrConsoleViewModel;

/**
 * Консоль HR: CRUD отделов, сотрудников и шаблонов заявок. Доступ только при роли HR.
 * Пункт меню «Синхронизация» тянет снимок справочников с REST (см. {@link com.ozhd.kadrov.employeesupport.data.remote.ApiService#getHrCatalog()}).
 */
public class HrConsoleFragment extends Fragment {

    public static HrConsoleFragment newInstance() {
        return new HrConsoleFragment();
    }

    private FragmentHrConsoleBinding binding;
    private TabLayoutMediator mediator;
    private HrConsoleViewModel consoleViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!RoleHelper.ensureHrOrToast(this)) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            });
            return new FrameLayout(requireContext());
        }
        binding = FragmentHrConsoleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) {
            return;
        }
        consoleViewModel = new ViewModelProvider(this).get(HrConsoleViewModel.class);
        binding.hrToolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
        binding.hrToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_hr_sync) {
                consoleViewModel.syncCatalogFromServer();
                return true;
            }
            return false;
        });
        consoleViewModel.getSyncOutcome().observe(getViewLifecycleOwner(), outcome -> {
            if (outcome == null) {
                return;
            }
            if (outcome.success) {
                Toast.makeText(requireContext(), R.string.hr_sync_ok, Toast.LENGTH_SHORT).show();
            } else {
                String detail = outcome.errorDetail != null ? outcome.errorDetail : "";
                Toast.makeText(requireContext(),
                        getString(R.string.hr_sync_fail, detail),
                        Toast.LENGTH_LONG).show();
            }
            consoleViewModel.clearSyncOutcome();
        });

        binding.hrViewPager.setAdapter(new HrPagerAdapter(this));
        binding.hrViewPager.setOffscreenPageLimit(4);
        mediator = new TabLayoutMediator(binding.hrTabLayout, binding.hrViewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.hr_tab_departments);
                            break;
                        case 1:
                            tab.setText(R.string.hr_tab_employees);
                            break;
                        case 2:
                            tab.setText(R.string.hr_tab_registrations);
                            break;
                        default:
                            tab.setText(R.string.hr_tab_request_types);
                            break;
                    }
                });
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
