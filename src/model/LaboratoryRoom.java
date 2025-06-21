package model;

public class LaboratoryRoom extends Room {
    private int workingPCs;

    public LaboratoryRoom(String roomName, int workingPCs) {
        super(roomName);
        this.workingPCs = workingPCs;
    }

    public int getWorkingPCs() {
        return workingPCs;
    }

    public void setWorkingPCs(int workingPCs) {

        this.workingPCs = workingPCs;
    }
}
