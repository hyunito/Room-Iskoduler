package model;

public class Room {
    protected String roomName;
    protected int chairs;
    protected boolean isOccupied;

    public Room(String roomName, int chairs) {
        this.roomName = roomName;
        this.chairs = chairs;
        this.isOccupied = false;
    }
    public String getRoomName() {
        return roomName;
    }

}