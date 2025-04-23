package model;

public class LaboratoryRoom extends Room {
    private int workingPCs;

    public LaboratoryRoom(String roomName, int chairs, int workingPCs) {
        super(roomName, chairs);
        this.workingPCs = workingPCs;
    }
}
