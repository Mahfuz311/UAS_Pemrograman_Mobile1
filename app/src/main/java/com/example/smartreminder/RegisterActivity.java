package com.example.smartreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmailReg, etPasswordReg, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Inisialisasi View
        etFullName = findViewById(R.id.etFullName);
        etEmailReg = findViewById(R.id.etEmailReg);
        etPasswordReg = findViewById(R.id.etPasswordReg);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // 2. Aksi Tombol Daftar
        btnRegister.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim(); // trim() menghapus spasi di awal/akhir
            String email = etEmailReg.getText().toString().trim();
            String pass = etPasswordReg.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            // Validasi Input
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(RegisterActivity.this, "Password tidak sama!", Toast.LENGTH_SHORT).show();
            } else {
                // --- SIMPAN DATA PENGGUNA ---
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("userName", name);     // Simpan Nama untuk ditampilkan di Profil & Home
                editor.putString("userEmail", email);   // Simpan Email
                editor.putString("userPassword", pass); // Simpan Password
                editor.putBoolean("isLoggedIn", true);  // Langsung set status login TRUE

                editor.apply();

                Toast.makeText(RegisterActivity.this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show();

                // Langsung masuk ke MainActivity (Beranda)
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                // Hapus history (agar saat tombol back ditekan, tidak kembali ke halaman register)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // 3. Aksi Link "Sudah punya akun? Masuk"
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}