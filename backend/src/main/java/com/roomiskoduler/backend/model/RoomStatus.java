package com.roomiskoduler.backend.model;

public class RoomStatus {
    private String roomName;
    private String roomType;
    private boolean occupied;
    private String statusText;

    public RoomStatus() {}

    public RoomStatus(String roomName, String roomType, boolean occupied, String statusText) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.occupied = occupied;
        this.statusText = statusText;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
} 