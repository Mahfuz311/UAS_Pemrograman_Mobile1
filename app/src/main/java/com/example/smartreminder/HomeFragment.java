package com.example.smartreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // UI Header
    private TextView tvGreeting, tvDate, tvDateSchedule;
    private FloatingActionButton fabAdd;

    // UI Jadwal Hari Ini
    private TextView tvEmptyScheduleHome;
    private LinearLayout containerHomeSchedule;

    // UI Tugas
    private TextView tvEmptyTask;
    private RecyclerView rvTaskList;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inisialisasi UI
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        tvDateSchedule = view.findViewById(R.id.tvDateSchedule);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Init Komponen Jadwal
        tvEmptyScheduleHome = view.findViewById(R.id.tvEmptyScheduleHome);
        containerHomeSchedule = view.findViewById(R.id.containerHomeSchedule);

        // Init Komponen Tugas
        tvEmptyTask = view.findViewById(R.id.tvEmptyTask);
        rvTaskList = view.findViewById(R.id.rvTaskList);

        // Setup RecyclerView Tugas
        rvTaskList.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();

        // Adapter
        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteTask(position);
            }

            @Override
            public void onProgressChanged(int position, int progress) {
                saveProgress(position, progress);
            }
        });

        rvTaskList.setAdapter(taskAdapter);

        // Load Semua Data
        loadUserData();
        loadTaskData();
        loadTodaySchedule();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTaskActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTaskData();
        loadTodaySchedule();
    }

    // --- LOGIKA JADWAL KULIAH HARI INI ---
    private void loadTodaySchedule() {
        String todayName = getTodayName();
        containerHomeSchedule.removeAllViews();

        SharedPreferences prefs = requireActivity().getSharedPreferences("ScheduleData", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("scheduleList", null);
        ArrayList<Schedule> fullList;

        if (json == null) {
            fullList = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<Schedule>>() {}.getType();
            fullList = gson.fromJson(json, type);
        }

        boolean found = false;
        for (Schedule item : fullList) {
            if (item.getDay().equalsIgnoreCase(todayName)) {
                found = true;

                View itemView = getLayoutInflater().inflate(R.layout.item_schedule, containerHomeSchedule, false);

                TextView tvTime = itemView.findViewById(R.id.tvItemTime);
                TextView tvSubject = itemView.findViewById(R.id.tvItemSubject);
                TextView tvDetails = itemView.findViewById(R.id.tvItemDetails);
                ImageView btnDelete = itemView.findViewById(R.id.btnDelete);

                tvTime.setText(item.getTime());
                tvSubject.setText(item.getSubject());
                tvDetails.setText(item.getRoom() + " • " + item.getLecturer());

                btnDelete.setVisibility(View.GONE);

                containerHomeSchedule.addView(itemView);
            }
        }

        if (found) {
            tvEmptyScheduleHome.setVisibility(View.GONE);
            containerHomeSchedule.setVisibility(View.VISIBLE);
        } else {
            tvEmptyScheduleHome.setVisibility(View.VISIBLE);
            tvEmptyScheduleHome.setText("Tidak ada jadwal kuliah hari " + todayName);
            containerHomeSchedule.setVisibility(View.GONE);
        }
    }

    private String getTodayName() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY: return "Minggu";
            case Calendar.MONDAY: return "Senin";
            case Calendar.TUESDAY: return "Selasa";
            case Calendar.WEDNESDAY: return "Rabu";
            case Calendar.THURSDAY: return "Kamis";
            case Calendar.FRIDAY: return "Jumat";
            case Calendar.SATURDAY: return "Sabtu";
            default: return "Senin";
        }
    }

    // --- LOGIKA TUGAS & USER ---
    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedName = prefs.getString("userName", "Pengguna");
        tvGreeting.setText("Halo, " + savedName + " 👋");

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
        tvDateSchedule.setText(currentDate);
    }

    // --- PERBAIKAN UTAMA: FILTER TUGAS KADALUARSA ---
    private void loadTaskData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("taskList", null);

        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        ArrayList<Task> savedTasks = gson.fromJson(json, type);

        taskList.clear();

        // Siapkan Tanggal Hari Ini (Tanpa Jam)
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", new Locale("id", "ID"));

        if (savedTasks != null) {
            for (Task task : savedTasks) {
                try {
                    // Konversi Deadline Tugas ke Date
                    Date deadlineDate = sdf.parse(task.getDate());

                    // LOGIKA:
                    // Jika Deadline >= Hari Ini -> TAMPILKAN
                    // Jika Deadline < Hari Ini -> JANGAN TAMPILKAN (KADALUARSA)
                    if (deadlineDate != null && !deadlineDate.before(today)) {
                        taskList.add(task);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Jika format tanggal salah, tetap tampilkan agar aman
                    taskList.add(task);
                }
            }
        }

        if (taskList.isEmpty()) {
            rvTaskList.setVisibility(View.GONE);
            tvEmptyTask.setVisibility(View.VISIBLE);
        } else {
            rvTaskList.setVisibility(View.VISIBLE);
            tvEmptyTask.setVisibility(View.GONE);
        }
        taskAdapter.notifyDataSetChanged();
    }

    private void deleteTask(int position) {
        taskList.remove(position);
        taskAdapter.notifyItemRemoved(position);
        taskAdapter.notifyItemRangeChanged(position, taskList.size());

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("taskList", json);
        editor.apply();

        if (taskList.isEmpty()) {
            rvTaskList.setVisibility(View.GONE);
            tvEmptyTask.setVisibility(View.VISIBLE);
        }
        Toast.makeText(getContext(), "Tugas dihapus", Toast.LENGTH_SHORT).show();
    }

    private void saveProgress(int position, int progress) {
        if (position >= 0 && position < taskList.size()) {
            taskList.get(position).setProgress(progress);

            SharedPreferences prefs = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(taskList);
            editor.putString("taskList", json);
            editor.apply();
        }
    }
}