package com.roomiskoduler.backend.data;
import com.roomiskoduler.backend.model.Room;

public class RoomLinkedList {
    private RoomNode head;

    public void add(Room room) {
        RoomNode newNode = new RoomNode(room);
        if (head == null) {
            head = newNode;
        } else {
            RoomNode current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    public void displayAllRooms() {
        RoomNode current = head;
        while (current != null) {
            System.out.println("Room: " + current.data.getRoomName());
            current = current.next;
        }
    }

    public RoomNode getHead() {
        return head;
    }

    public Room[] toArray() {
        int size = 0;
        RoomNode current = head;
        while (current != null) {
            size++;
            current = current.next;
        }

        Room[] rooms = new Room[size];
        current = head;
        int index = 0;
        while (current != null) {
            rooms[index++] = current.data;
            current = current.next;
        }

        return rooms;
    }
}

