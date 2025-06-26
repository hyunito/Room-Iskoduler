package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.data.RoomNode;
import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.model.Room;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;  // <- import this
import java.util.HashMap;

@Service
public class RoomHashMap {

    private final HashMap<String, Room> map = new HashMap<>();
    @PostConstruct
    public void init() {
        RoomLinkedList list = RoomFinderDAO.getAllRooms();
        RoomNode current = list.getHead();
        while (current != null) {
            map.put(current.data.getRoomName(), current.data);
            current = current.next;
        }
        System.out.println("✅ RoomHashMap loaded with " + map.size() + " rooms.");
    }

    public Room getRoomByName(String name) {
        return map.get(name);
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public void displayAllRoomKeys() {
        map.keySet().forEach(key -> System.out.println("- " + key));
    }
}
