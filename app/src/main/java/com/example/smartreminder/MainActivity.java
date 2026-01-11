package com.example.smartreminder;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Cek Tema Gelap
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        // 2. Terapkan Warna Status Bar dengan Penundaan (Agar tidak tertimpa sistem)
        applyStatusBarColor();

        // 3. Izin Notifikasi
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            activeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, activeFragment)
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_schedule) {
                    selectedFragment = new ScheduleFragment();
                } else if (item.getItemId() == R.id.nav_notification) {
                    selectedFragment = new NotificationFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    activeFragment = selectedFragment;
                }
                return true;
            }
        });

        bottomNav.setOnItemReselectedListener(item -> {});
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyStatusBarColor();
    }

    // --- METODE BARU YANG LEBIH KUAT ---
    private void applyStatusBarColor() {
        Window window = getWindow();
        View decorView = window.getDecorView();

        // Gunakan .post() agar kode ini dijalankan SETELAH sistem selesai menggambar tema
        decorView.post(() -> {
            try {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.parseColor("#2979FF"));

                // Matikan Light Status Bar (Agar ikon putih)
                int flags = decorView.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(flags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}