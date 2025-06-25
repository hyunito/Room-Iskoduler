package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.model.LaboratoryRoom;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*") // Allow all frontend origins
public class RoomController {

    @GetMapping
    public List<Room> getAllRooms() {
        // Use existing DAO to fetch unoccupied rooms (can be adjusted to fetch all)
        return RoomFinderDAO.getAllUnoccupiedRooms().toList();  // You may need to implement toList() in RoomLinkedList
    }

    @GetMapping("/status/{roomName}")
    public RoomStatusResponse getRoomStatus(@PathVariable String roomName) {
        boolean occupied = RoomFinderDAO.isRoomCurrentlyOccupied(roomName);

        String type = RoomFinderDAO.getRoomType(roomName);
        if (type != null && !type.isEmpty()) {
            type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        }


        String statusText = occupied ? "Currently Occupied" : "Available";

        RoomStatusResponse response = new RoomStatusResponse();
        response.setRoomName(roomName);
        response.setRoomType(type);
        response.setOccupied(occupied);
        response.setStatusText(statusText);

        return response;
    }

    // Inner class for response formatting
    public static class RoomStatusResponse {
        private String roomName;
        private String roomType;
        private boolean occupied;
        private String statusText;

        // Getters and setters
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }

        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }

        public boolean isOccupied() { return occupied; }
        public void setOccupied(boolean occupied) { this.occupied = occupied; }

        public String getStatusText() { return statusText; }
        public void setStatusText(String statusText) { this.statusText = statusText; }
    }
}
