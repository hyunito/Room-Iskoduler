package com.roomiskoduler.backend.model;

public class RoomRequestPayload {


    private String date;
    private String time;
    private int duration;
    private String role;

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
