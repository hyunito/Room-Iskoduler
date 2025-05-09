package main;


import data.RoomLinkedList;
import model.LaboratoryRoom;
import data.RoomNode;


public class Main {
    public static void main(String[] args) {
        LaboratoryRoom lab = new LaboratoryRoom("S501", 40, 30);
        RoomLinkedList rooms = new RoomLinkedList();
        RoomNode node = new RoomNode(lab);
        rooms.add(lab);
        rooms.displayAllRooms();
    }
}
