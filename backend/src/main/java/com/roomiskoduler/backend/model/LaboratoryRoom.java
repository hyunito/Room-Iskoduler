package com.roomiskoduler.backend.model;

public class LaboratoryRoom extends Room {
    private int workingPCs;
    private int capacity; // total number of seats/chairs in the lab

    public LaboratoryRoom(String roomName, int workingPCs, int capacity) {
        super(roomName);
        this.workingPCs = workingPCs;
        this.capacity = capacity;
    }

    public int getWorkingPCs() {
        return workingPCs;
    }

    public void setWorkingPCs(int workingPCs) {
        this.workingPCs = workingPCs;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }
}
