package com.example.smartreminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationFragment extends Fragment {

    private TextView tvSubtitle;
    private CardView cardEmptyState;
    private RecyclerView rvNotification;
    private TaskAdapter notificationAdapter;
    private ArrayList<Task> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        cardEmptyState = view.findViewById(R.id.cardEmptyState);
        rvNotification = view.findViewById(R.id.rvNotification);

        rvNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();

        notificationAdapter = new TaskAdapter(notificationList, null); // Null listener krn cuma view
        rvNotification.setAdapter(notificationAdapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("taskList", null);

        if (json == null) return;

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        ArrayList<Task> allTasks = gson.fromJson(json, type);

        notificationList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", new Locale("id", "ID"));

        // Tanggal Hari Ini (Reset jam ke 00:00)
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();

        if (allTasks != null) {
            for (Task task : allTasks) {
                try {
                    // 1. Hitung Sisa Hari (Deadline - Hari Ini)
                    Date deadlineDate = sdf.parse(task.getDate());
                    if (deadlineDate != null) {
                        long diffInMillis = deadlineDate.getTime() - today.getTime();
                        long daysUntilDeadline = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                        // 2. Ambil Settingan Pengingat User
                        int triggerDay = convertReminderToDays(task.getReminder());

                        // 3. LOGIKA UTAMA
                        // Tampilkan jika: Sisa hari <= Trigger Pengingat
                        // DAN Sisa hari >= 0 (Belum lewat deadline)
                        if (daysUntilDeadline <= triggerDay && daysUntilDeadline >= 0) {
                            notificationList.add(task);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (notificationList.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            rvNotification.setVisibility(View.GONE);
            tvSubtitle.setText("0 pengingat aktif");
        } else {
            cardEmptyState.setVisibility(View.GONE);
            rvNotification.setVisibility(View.VISIBLE);
            tvSubtitle.setText(notificationList.size() + " pengingat aktif");
        }

        notificationAdapter.notifyDataSetChanged();
    }

    // Konversi Teks "1 hari sebelum" menjadi angka 1
    private int convertReminderToDays(String reminder) {
        if (reminder == null) return 0; // Default hari H
        if (reminder.contains("4 hari")) return 4;
        if (reminder.contains("3 hari")) return 3;
        if (reminder.contains("2 hari")) return 2;
        if (reminder.contains("1 hari")) return 1;
        // Untuk "15 menit" dan "1 jam", dianggap 0 (Hari H)
        return 0;
    }
}