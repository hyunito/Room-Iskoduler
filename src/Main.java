package model;
import data.RoomNode;
import logic.SequentialSearch;

public class Main {
    public static void main(String[] args) {

        LaboratoryRoom lab = new LaboratoryRoom("S501", 40, 30);
        RoomNode node = new RoomNode(lab);
        System.out.print("The available laboratory rooms are: ");

        }
    }
