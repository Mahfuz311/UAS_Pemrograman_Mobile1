package com.example.smartreminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerReminder;
    private TextView tvDeadline;
    private Button btnSaveTask;
    private EditText etTaskName, etMatkul, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        try {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#2979FF"));
        } catch (Exception e) { e.printStackTrace(); }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        btnBack = findViewById(R.id.btnBack);
        etMatkul = findViewById(R.id.etMatkul);
        spinnerReminder = findViewById(R.id.spinnerReminder);
        tvDeadline = findViewById(R.id.tvDeadline);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        etTaskName = findViewById(R.id.etTaskName);
        etNotes = findViewById(R.id.etNotes);

        // Setup Spinner Reminder
        String[] reminderList = {"15 menit sebelum", "1 jam sebelum", "1 hari sebelum", "2 hari sebelum", "3 hari sebelum", "4 hari sebelum"};
        ArrayAdapter<String> adapterReminder = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reminderList);
        spinnerReminder.setAdapter(adapterReminder);

        tvDeadline.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());

        // LOGIKA TOMBOL SIMPAN
        btnSaveTask.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString();
            String matkul = etMatkul.getText().toString();
            String date = tvDeadline.getText().toString();
            String notes = etNotes.getText().toString();
            String reminder = spinnerReminder.getSelectedItem().toString();

            if (taskName.isEmpty() || matkul.isEmpty() || date.equals("")) {
                Toast.makeText(this, "Lengkapi data!", Toast.LENGTH_SHORT).show();
            } else {
                saveTaskToList(taskName, matkul, date, notes, reminder);

                scheduleNotification(taskName, date, reminder);
            }
        });
    }

    private void saveTaskToList(String title, String matkul, String date, String notes, String reminder) {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("taskList", null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        ArrayList<Task> taskList = gson.fromJson(json, type);
        if (taskList == null) taskList = new ArrayList<>();
        taskList.add(new Task(title, matkul, date, notes, reminder));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("taskList", gson.toJson(taskList));
        editor.apply();
        Toast.makeText(this, "Tugas berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, day1) -> {
                    String selectedDate = day1 + "/" + (month1 + 1) + "/" + year1;
                    tvDeadline.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void scheduleNotification(String title, String deadlineDate, String reminderType) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", new Locale("id", "ID"));
        try {
            Date date = sdf.parse(deadlineDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // 1. Tentukan jam berapa notifikasi bunyi (Misal: Jam 08:00 Pagi)
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // 2. Kurangi tanggal sesuai pilihan user
            int daysToSubtract = 0;
            if (reminderType.contains("1 hari")) daysToSubtract = 1;
            else if (reminderType.contains("2 hari")) daysToSubtract = 2;
            else if (reminderType.contains("3 hari")) daysToSubtract = 3;
            else if (reminderType.contains("4 hari")) daysToSubtract = 4;

            // Kurangi hari
            calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                return;
            }

            // 3. Set Alarm
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "Pengingat Tugas: " + title);
            intent.putExtra("message", "Deadline tugas " + title + " tinggal " + reminderType + " lagi!");

            int uniqueId = (int) System.currentTimeMillis();
            intent.putExtra("id", uniqueId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE);

            // Pasang Alarm
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}