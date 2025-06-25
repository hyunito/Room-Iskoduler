package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.data.RoomNode;
import com.roomiskoduler.backend.model.Room;

import java.util.HashMap;

public class RoomHashMap {

    private HashMap<String, Room> map;

    public RoomHashMap(RoomLinkedList list) {
        map = new HashMap<>();
        RoomNode current = list.getHead();
        while (current != null) {
            map.put(current.data.getRoomName(), current.data);
            current = current.next;
        }
    }

    public Room getRoomByName(String name) {
        return map.get(name);
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public void displayAllRoomKeys() {
        for (String key : map.keySet()) {
            System.out.println("- " + key);
        }
    }
}
