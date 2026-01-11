package com.example.smartreminder;

import android.content.Intent;
import android.content.SharedPreferences; // Import ini penting
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // --- LOGIKA LOGIN YANG DIPERBARUI ---
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmail = etEmail.getText().toString();
                String inputPassword = etPassword.getText().toString();

                // 1. Validasi Kolom Kosong
                if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show();
                    return; // Berhenti di sini
                }

                // 2. AMBIL DATA YANG TERSIMPAN DI HP
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String registeredEmail = sharedPreferences.getString("userEmail", null);
                String registeredPassword = sharedPreferences.getString("userPassword", null);

                // 3. CEK KECOCOKAN DATA
                if (registeredEmail == null || registeredPassword == null) {
                    // Artinya belum pernah ada yang daftar di HP ini
                    Toast.makeText(LoginActivity.this, "Belum ada akun terdaftar. Silakan daftar dulu!", Toast.LENGTH_LONG).show();
                }
                else if (inputEmail.equals(registeredEmail) && inputPassword.equals(registeredPassword)) {
                    // JIKA EMAIL SAMA DAN PASSWORD SAMA -> SUKSES
                    Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    // JIKA SALAH SATU TIDAK COCOK -> GAGAL
                    Toast.makeText(LoginActivity.this, "Email atau Password salah!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}