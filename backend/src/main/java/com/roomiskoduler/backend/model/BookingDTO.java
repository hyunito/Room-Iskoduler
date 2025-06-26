package com.roomiskoduler.backend.model;

public class BookingDTO {
    private String roomName;
    private String date;
    private String timeIn;
    private String timeOut;
    private String status;

    public BookingDTO(String roomName, String date, String timeIn, String timeOut, String status) {
        this.roomName = roomName;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getDate() {
        return date;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public String getStatus() {
        return status;
    }

    // Optional: setters, toString, equals, hashCode if needed later
}
