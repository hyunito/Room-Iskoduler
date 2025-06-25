package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.data.RoomLinkedList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    public List<Room> getAllRooms() {
        RoomLinkedList list = RoomFinderDAO.getAllUnoccupiedRooms();
        Room[] array = list.toArray();
        List<Room> rooms = new ArrayList<>();
        for (Room room : array) {
            rooms.add(room);
        }
        return rooms;
    }

    public Room getRoomByName(String name) {
        List<Room> all = getAllRooms();
        for (Room room : all) {
            if (room.getRoomName().equalsIgnoreCase(name)) {
                return room;
            }
        }
        return null;
    }
}
