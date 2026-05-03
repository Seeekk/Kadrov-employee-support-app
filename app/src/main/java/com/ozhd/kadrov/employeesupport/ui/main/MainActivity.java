package com.ozhd.kadrov.employeesupport.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.RoleHelper;
import com.ozhd.kadrov.employeesupport.ui.hr.HrConsoleFragment;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.SessionViewModel;
import com.ozhd.kadrov.employeesupport.databinding.ActivityMainBinding;
import com.ozhd.kadrov.employeesupport.ui.auth.AuthUiHelper;
import com.ozhd.kadrov.employeesupport.ui.employees.EmployeesFragment;
import com.ozhd.kadrov.employeesupport.ui.home.HomeFragment;
import com.ozhd.kadrov.employeesupport.ui.notifications.NotificationsFragment;
import com.ozhd.kadrov.employeesupport.ui.requests.CreateRequestFragment;

/**
 * Контейнер с BottomNavigationView: замена корневых фрагментов по выбранному разделу.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.inflateMenu(R.menu.menu_main);
        binding.toolbar.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));

        SessionViewModel sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        sessionViewModel.getRole().observe(this, role -> {
            if (binding == null) return;
            android.view.MenuItem hr = binding.toolbar.getMenu().findItem(R.id.menu_hr_console);
            if (hr != null) {
                hr.setVisible(RoleHelper.isHr(role));
            }
        });

        if (savedInstanceState == null) {
            openFragment(HomeFragment.newInstance(), R.id.nav_home);
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                openFragment(HomeFragment.newInstance(), id);
                return true;
            }
            if (id == R.id.nav_employees) {
                openFragment(EmployeesFragment.newInstance(), id);
                return true;
            }
            if (id == R.id.nav_create) {
                openFragment(CreateRequestFragment.newInstance(), id);
                return true;
            }
            if (id == R.id.nav_notifications) {
                openFragment(NotificationsFragment.newInstance(), id);
                return true;
            }
            return false;
        });

        binding.bottomNav.setOnItemReselectedListener(item -> { /* без сброса стека */ });
    }

    private void openFragment(@NonNull Fragment fragment, @IdRes int navId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_hr_console) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, HrConsoleFragment.newInstance())
                    .addToBackStack("hr_console")
                    .commit();
            return true;
        }
        if (id == R.id.menu_logout) {
            AuthUiHelper.showLogoutDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
