package logic;

import data.RoomLinkedList;
import data.RoomNode;
import model.Room;

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
