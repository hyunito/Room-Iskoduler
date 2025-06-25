package com.roomiskoduler.backend.model;

public class Room {
    protected String roomName;
    protected boolean isOccupied;

    public Room(String roomName) {
        this.roomName = roomName;
        this.isOccupied = false;
    }
    public String getRoomName() {
        return roomName;
    }
    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }


}