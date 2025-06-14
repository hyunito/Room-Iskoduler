package main;


import data.RoomLinkedList;
import model.LaboratoryRoom;
import model.Room;
import data.RoomNode;


public class Main {
    public static void main(String[] args) {


        RoomLinkedList rooms = new RoomLinkedList();
        rooms.add(new LaboratoryRoom("S501", 40));
        rooms.add(new LaboratoryRoom("S502", 40));
        rooms.add(new LaboratoryRoom("S503", 40));
        rooms.add(new LaboratoryRoom("S504", 40));
        rooms.add(new Room("S504"));
        rooms.displayAllRooms();
    }
}
