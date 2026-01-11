package com.example.smartreminder;

public class Task {
    private String title;
    private String matkul;
    private String date;
    private String notes;
    private String reminder;
    private int progress; // <--- TAMBAHAN BARU

    // Constructor Diupdate (tambah progress, default 0 di awal)
    public Task(String title, String matkul, String date, String notes, String reminder) {
        this.title = title;
        this.matkul = matkul;
        this.date = date;
        this.notes = notes;
        this.reminder = reminder;
        this.progress = 0; // Default 0%
    }

    public String getTitle() { return title; }
    public String getMatkul() { return matkul; }
    public String getDate() { return date; }
    public String getNotes() { return notes; }
    public String getReminder() { return reminder; }

    // Getter & Setter untuk Progress
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}