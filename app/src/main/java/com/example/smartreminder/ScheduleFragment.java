package com.example.smartreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    private LinearLayout btnAddSchedule;
    private TextView tvEmptySchedule;
    private LinearLayout containerSchedule; // Wadah untuk menempelkan item

    // Tab Hari (Sekarang sudah ada Sabtu & Minggu)
    private TextView tabSenin, tabSelasa, tabRabu, tabKamis, tabJumat, tabSabtu, tabMinggu;
    private String selectedDay = "Senin";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        btnAddSchedule = view.findViewById(R.id.btnAddSchedule);
        tvEmptySchedule = view.findViewById(R.id.tvEmptySchedule);
        containerSchedule = view.findViewById(R.id.containerSchedule);

        // Inisialisasi Tab
        tabSenin = view.findViewById(R.id.tabSenin);
        tabSelasa = view.findViewById(R.id.tabSelasa);
        tabRabu = view.findViewById(R.id.tabRabu);
        tabKamis = view.findViewById(R.id.tabKamis);
        tabJumat = view.findViewById(R.id.tabJumat);
        tabSabtu = view.findViewById(R.id.tabSabtu);   // Baru
        tabMinggu = view.findViewById(R.id.tabMinggu); // Baru

        setupTabs();
        loadScheduleList(); // Panggil fungsi load

        btnAddSchedule.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddScheduleActivity.class));
        });

        return view;
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            resetTabStyle();
            TextView t = (TextView) v;
            t.setBackgroundResource(R.drawable.bg_white_pill);
            t.setTextColor(Color.parseColor("#2979FF"));

            // Set hari sesuai tab yang diklik
            if (v == tabSenin) selectedDay = "Senin";
            else if (v == tabSelasa) selectedDay = "Selasa";
            else if (v == tabRabu) selectedDay = "Rabu";
            else if (v == tabKamis) selectedDay = "Kamis";
            else if (v == tabJumat) selectedDay = "Jumat";
            else if (v == tabSabtu) selectedDay = "Sabtu";   // Baru
            else if (v == tabMinggu) selectedDay = "Minggu"; // Baru

            loadScheduleList(); // Refresh list saat ganti hari
        };

        tabSenin.setOnClickListener(listener);
        tabSelasa.setOnClickListener(listener);
        tabRabu.setOnClickListener(listener);
        tabKamis.setOnClickListener(listener);
        tabJumat.setOnClickListener(listener);
        tabSabtu.setOnClickListener(listener);   // Baru
        tabMinggu.setOnClickListener(listener);  // Baru
    }

    private void resetTabStyle() {
        // Tambahkan Sabtu & Minggu ke dalam array reset
        TextView[] tabs = {tabSenin, tabSelasa, tabRabu, tabKamis, tabJumat, tabSabtu, tabMinggu};
        for (TextView t : tabs) {
            t.setBackground(null);
            t.setTextColor(Color.parseColor("#CCFFFFFF"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadScheduleList();
    }

    private void loadScheduleList() {
        // 1. Bersihkan tampilan lama
        containerSchedule.removeAllViews();

        // 2. Ambil List dari SharedPreferences
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

        // 3. Filter Sesuai Hari & Tampilkan
        boolean found = false;
        for (int i = 0; i < fullList.size(); i++) {
            Schedule item = fullList.get(i);

            // Cek apakah harinya cocok
            if (item.getDay().equalsIgnoreCase(selectedDay)) {
                found = true;

                // Inflate layout item_schedule secara manual
                View itemView = getLayoutInflater().inflate(R.layout.item_schedule, containerSchedule, false);

                // Isi datanya
                TextView tvTime = itemView.findViewById(R.id.tvItemTime);
                TextView tvSubject = itemView.findViewById(R.id.tvItemSubject);
                TextView tvDetails = itemView.findViewById(R.id.tvItemDetails);
                ImageView btnDelete = itemView.findViewById(R.id.btnDelete);

                tvTime.setText(item.getTime());
                tvSubject.setText(item.getSubject());
                tvDetails.setText(item.getRoom() + " • " + item.getLecturer());

                // Logika Hapus per Item
                final int index = i;
                btnDelete.setOnClickListener(v -> deleteItem(index));

                // Tempelkan ke wadah
                containerSchedule.addView(itemView);
            }
        }

        // Atur tampilan Kosong/Isi
        if (found) {
            tvEmptySchedule.setVisibility(View.GONE);
        } else {
            tvEmptySchedule.setVisibility(View.VISIBLE);
        }
    }

    private void deleteItem(int index) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ScheduleData", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("scheduleList", null);
        Type type = new TypeToken<ArrayList<Schedule>>() {}.getType();
        ArrayList<Schedule> list = gson.fromJson(json, type);

        // Hapus item dari list
        if (list != null && index < list.size()) {
            list.remove(index);

            // Simpan ulang list yang sudah dikurangi
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("scheduleList", gson.toJson(list));
            editor.apply();

            Toast.makeText(getActivity(), "Dihapus", Toast.LENGTH_SHORT).show();
            loadScheduleList(); // Refresh tampilan
        }
    }
}