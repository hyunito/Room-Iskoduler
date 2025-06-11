package model;

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

}