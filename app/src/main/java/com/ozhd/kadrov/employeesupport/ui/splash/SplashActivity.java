package com.ozhd.kadrov.employeesupport.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.ui.auth.LoginActivity;
import com.ozhd.kadrov.employeesupport.ui.main.MainActivity;

/**
 * Загрузочный экран при входе в приложение: фон (ж/д тематика), красная надпись «Кадровый центр ОЖД»,
 * затем проверка сессии и переход в {@link MainActivity} или {@link LoginActivity}.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long DISPLAY_MS = 1800L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goNext = this::navigateBySession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goNext, DISPLAY_MS);
    }

    private void navigateBySession() {
        if (isFinishing()) {
            return;
        }
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goNext);
        super.onDestroy();
    }
}
