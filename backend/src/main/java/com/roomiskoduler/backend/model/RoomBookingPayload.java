package com.roomiskoduler.backend.model;

public class RoomBookingPayload {
    private String room;
    private String date;
    private String time;
    private int duration;
    private String role;

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
