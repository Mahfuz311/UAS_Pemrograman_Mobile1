package com.example.smartreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private ImageView imgProfile;
    private Button btnLogout;
    private LinearLayout btnChangeProfile;

    // Variabel Settings
    private SwitchMaterial switchDarkMode;
    private RadioGroup radioGroupNotif;
    private RadioButton rbPopup, rbSilent;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inisialisasi
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangeProfile = view.findViewById(R.id.btnChangeProfile);

        // Init Settings
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        radioGroupNotif = view.findViewById(R.id.radioGroupNotif);
        rbPopup = view.findViewById(R.id.rbPopup);
        rbSilent = view.findViewById(R.id.rbSilent);

        // --- SETUP LOGIKA PENGATURAN ---
        setupSettings();

        // Setup Launcher Galeri
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                requireActivity().getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) { e.printStackTrace(); }
                            imgProfile.setImageURI(selectedImageUri);
                            saveProfileImage(selectedImageUri.toString());
                        }
                    }
                }
        );

        try { loadUserData(); } catch (Exception e) { e.printStackTrace(); }

        if (btnChangeProfile != null) {
            btnChangeProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getActivity() != null) {
                    SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", false);
                    editor.apply();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    private void setupSettings() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        // 1. TEMA GELAP (Load state)
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        switchDarkMode.setChecked(isDarkMode);

        // Listener Switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isDarkMode", isChecked);
            editor.apply();

            // Ubah Tema Secara Langsung
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // 2. MODE NOTIFIKASI (Load state)
        boolean isSilent = prefs.getBoolean("isSilentMode", false);
        if (isSilent) {
            rbSilent.setChecked(true);
        } else {
            rbPopup.setChecked(true);
        }

        // Listener Radio Group
        radioGroupNotif.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            if (checkedId == R.id.rbSilent) {
                editor.putBoolean("isSilentMode", true);
                Toast.makeText(getContext(), "Mode Silent Aktif", Toast.LENGTH_SHORT).show();
            } else {
                editor.putBoolean("isSilentMode", false);
                Toast.makeText(getContext(), "Mode Popup Aktif", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        });
    }

    private void loadUserData() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "Pengguna");
        String email = prefs.getString("userEmail", "email@contoh.com");
        if (tvUserName != null) tvUserName.setText(name);
        if (tvUserEmail != null) tvUserEmail.setText(email);
        String imageUriString = prefs.getString("profileImage", null);
        if (imageUriString != null && imgProfile != null) {
            try { imgProfile.setImageURI(Uri.parse(imageUriString)); }
            catch (Exception e) { imgProfile.setImageResource(R.mipmap.ic_launcher); }
        }
    }

    private void saveProfileImage(String uriString) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("profileImage", uriString);
        editor.apply();
    }
}