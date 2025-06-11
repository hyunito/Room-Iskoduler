package model;

public class LaboratoryRoom extends Room {
    private int workingPCs;

    public LaboratoryRoom(String roomName, int workingPCs) {
        super(roomName);
        this.workingPCs = workingPCs;
    }
}
