package com.roomiskoduler.backend.model;

public class BookingDTO {
    private String roomName;
    private String date;
    private String roomType;
    private String timeIn;
    private String timeOut;
    private String status;

    public BookingDTO(String roomName, String roomType, String date, String timeIn, String timeOut, String status) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    public String getRoomType() {
        return roomType;
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

}
