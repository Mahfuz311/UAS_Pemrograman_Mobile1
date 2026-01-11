package com.example.smartreminder;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color; // JANGAN LUPA IMPORT INI
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddScheduleActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etSubjectName, etRoom, etLecturer;
    private Spinner spinnerDay;
    private TextView tvStartTime, tvEndTime;
    private Button btnSaveSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        try {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#2979FF"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ---------------------------------------------------

        // Inisialisasi View
        btnBack = findViewById(R.id.btnBackSchedule);
        etSubjectName = findViewById(R.id.etSubjectName);
        etRoom = findViewById(R.id.etRoom);
        etLecturer = findViewById(R.id.etLecturer);
        spinnerDay = findViewById(R.id.spinnerDay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);

        // Setup Spinner
        String[] days = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
        spinnerDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));

        // Setup TimePicker
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));
        btnBack.setOnClickListener(v -> finish());

        // Logika Simpan
        btnSaveSchedule.setOnClickListener(v -> {
            String subject = etSubjectName.getText().toString();
            String day = spinnerDay.getSelectedItem().toString();
            String time = tvStartTime.getText().toString() + " - " + tvEndTime.getText().toString();
            String room = etRoom.getText().toString();
            String lecturer = etLecturer.getText().toString();

            if (subject.isEmpty() || room.isEmpty()) {
                Toast.makeText(this, "Lengkapi data!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("ScheduleData", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString("scheduleList", null);

            ArrayList<Schedule> scheduleList;
            if (json == null) {
                scheduleList = new ArrayList<>();
            } else {
                Type type = new TypeToken<ArrayList<Schedule>>() {}.getType();
                scheduleList = gson.fromJson(json, type);
            }

            scheduleList.add(new Schedule(subject, day, time, room, lecturer));

            SharedPreferences.Editor editor = prefs.edit();
            String updatedJson = gson.toJson(scheduleList);
            editor.putString("scheduleList", updatedJson);
            editor.apply();

            Toast.makeText(this, "Jadwal berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showTimePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) ->
                targetView.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
}