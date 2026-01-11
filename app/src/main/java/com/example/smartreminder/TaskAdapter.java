package com.example.smartreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onProgressChanged(int position, int progress);
    }

    public TaskAdapter(ArrayList<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvMatkul.setText(task.getMatkul());
        holder.tvDate.setText(task.getDate());

        // --- BAGIAN CATATAN (DIPERBARUI) ---
        // Menggunakan Emoji Pensil (📝) sebagai pengganti ikon drawable
        if (task.getNotes() == null || task.getNotes().isEmpty()) {
            holder.tvNotes.setText("📝 -");
        } else {
            holder.tvNotes.setText("📝 " + task.getNotes());
        }

        // --- LOGIKA PROGRESS BAR (SEEKBAR) ---
        holder.seekBarProgress.setProgress(task.getProgress());
        holder.tvProgressPercent.setText(task.getProgress() + "%");

        holder.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    holder.tvProgressPercent.setText(progress + "%");
                    task.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onProgressChanged(holder.getAdapterPosition(), seekBar.getProgress());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (taskList != null) ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMatkul, tvDate, tvNotes, tvProgressPercent;
        ImageView btnDelete;
        SeekBar seekBarProgress;

        public TaskViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvMatkul = itemView.findViewById(R.id.tvItemMatkul);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvNotes = itemView.findViewById(R.id.tvItemNotes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            seekBarProgress = itemView.findViewById(R.id.seekBarProgress);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}