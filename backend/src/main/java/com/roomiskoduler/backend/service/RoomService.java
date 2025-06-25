package com.roomiskoduler.backend.service;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.model.Room;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    public Room getRoomByName(String roomName) {
        Room room = RoomFinderDAO.findRoomByName(roomName);
        if (room == null) return null;

        boolean isOccupied = RoomFinderDAO.isRoomCurrentlyOccupied(roomName);
        room.setOccupied(isOccupied);
        return room;
    }
}
