package com.example.smartreminder;

public class Schedule {
    private String subject;
    private String day;
    private String time;
    private String room;
    private String lecturer;

    public Schedule(String subject, String day, String time, String room, String lecturer) {
        this.subject = subject;
        this.day = day;
        this.time = time;
        this.room = room;
        this.lecturer = lecturer;
    }

    // Getter methods
    public String getSubject() { return subject; }
    public String getDay() { return day; }
    public String getTime() { return time; }
    public String getRoom() { return room; }
    public String getLecturer() { return lecturer; }
}