package com.example.smartreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressLint("CustomSplashScreen") // Menghilangkan warning tentang splash screen bawaan Android 12
public class SplashActivity extends AppCompatActivity {

    // Komponen UI
    private TextView tvLoadingDots, tvCountryName, tvGreeting;
    private ImageView imgUserFlag;

    // Alat Pelacak Lokasi (Invisible)
    private FusedLocationProviderClient fusedLocationClient;

    // Handler Animasi (Ditambah 'final' agar warning hilang)
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int dotCount = 0;
    private Runnable dotRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Inisialisasi View
        tvLoadingDots = findViewById(R.id.tvLoadingDots);
        tvCountryName = findViewById(R.id.tvCountryName);
        tvGreeting = findViewById(R.id.tvGreeting);
        imgUserFlag = findViewById(R.id.imgUserFlag);

        // 2. Siapkan Client Lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 3. Jalankan Animasi Teks "Tracking..."
        startTrackingTextAnim();

        // 4. Mulai Cek Izin Lokasi dengan Delay 1 detik
        // Menggunakan Lambda "() ->" menggantikan "new Runnable()"
        handler.postDelayed(this::checkLocationPermission, 1000);
    }

    // --- CEK IZIN ---
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Minta Izin
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            // Izin sudah ada
            getLocationAndShowResult();
        }
    }

    // --- HASIL PERMINTAAN IZIN ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan
                getLocationAndShowResult();
            } else {
                // Izin ditolak, tampilkan default ID setelah delay singkat
                // Menggunakan Lambda
                handler.postDelayed(() -> showResultSequence("ID", "INDONESIA"), 1000);
            }
        }
    }

    // --- LOGIKA UTAMA: AMBIL DATA LOKASI ---
    @SuppressLint("MissingPermission")
    private void getLocationAndShowResult() {
        // Pengecekan Izin Manual (Double Check)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Menggunakan Lambda "location -> {}" menggantikan "new OnSuccessListener..."
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Default Value
            String code = "ID";
            String name = "INDONESIA";

            if (location != null) {
                // Lokasi ketemu! Gunakan Geocoder
                Geocoder geocoder = new Geocoder(SplashActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        code = addresses.get(0).getCountryCode();
                        // Menggunakan variable sementara untuk menghindari warning "assigned value"
                        String detectedName = addresses.get(0).getCountryName();
                        if (detectedName != null) {
                            name = detectedName.toUpperCase();
                        }
                    }
                } catch (IOException e) {
                    // Menggunakan Log.e menggantikan printStackTrace agar lebih bersih
                    Log.e("SplashActivity", "Geocoder error", e);
                }
            }

            final String finalCode = code;
            final String finalName = name;

            // Delay 2 detik agar animasi tracking sempat terlihat
            handler.postDelayed(() -> showResultSequence(finalCode, finalName), 2000);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showResultSequence(String countryCode, String countryName) {
        // 1. Matikan Animasi Tracking
        if (dotRunnable != null) {
            handler.removeCallbacks(dotRunnable);
        }
        tvLoadingDots.setVisibility(View.GONE);

        // 2. Siapkan Resource Gambar & Teks
        int flagRes = R.drawable.flag_us;
        String greetingStr = "HELLO 👋";

        if (countryCode != null) {
            if (countryCode.equalsIgnoreCase("ID")) {
                flagRes = R.drawable.flag_id;
                greetingStr = "HALO 👋";
            } else if (countryCode.equalsIgnoreCase("JP")) {
                flagRes = R.drawable.flag_jp;
                greetingStr = "KONNICHIWA 👋";
            } else if (countryCode.equalsIgnoreCase("GB")) {
                flagRes = R.drawable.flag_us;
                greetingStr = "HELLO 👋";
            }
        }

        // Set Data
        imgUserFlag.setImageResource(flagRes);
        tvCountryName.setText(countryName);
        tvGreeting.setText(greetingStr);


        // Tahap A: MUNCULKAN BENDERA & NAMA NEGARA
        imgUserFlag.setVisibility(View.VISIBLE);
        tvCountryName.setVisibility(View.VISIBLE);

        // Tahap B: 1.5 Detik kemudian -> GANTI NAMA JADI SAPAAN
        handler.postDelayed(() -> {
            tvCountryName.setVisibility(View.GONE);
            tvGreeting.setVisibility(View.VISIBLE);
        }, 1500);

        // Tahap C: 1.5 Detik lagi -> PINDAH KE MAIN
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }

    // --- ANIMASI TITIK TEKS ---
    @SuppressLint("SetTextI18n")
    private void startTrackingTextAnim() {
        dotRunnable = new Runnable() {
            @Override
            public void run() {
                String base = "Tracking Location";
                String dots = "";
                switch (dotCount % 4) {
                    case 0: dots = ""; break;
                    case 1: dots = "."; break;
                    case 2: dots = ".."; break;
                    case 3: dots = "..."; break;
                }
                tvLoadingDots.setText(base + dots);
                dotCount++;
                handler.postDelayed(this, 500);
            }
        };
        handler.post(dotRunnable);
    }
}