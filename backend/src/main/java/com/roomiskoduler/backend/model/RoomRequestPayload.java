package com.roomiskoduler.backend.model;

public class RoomRequestPayload {

    private String date;
    private String time;
    private int duration;
    private String role;

    private int pcs;
    private int students;
    private String roomType;

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getPcs() { return pcs; }
    public void setPcs(int pcs) { this.pcs = pcs; }

    public int getStudents() { return students; }
    public void setStudents(int students) { this.students = students; }
}
