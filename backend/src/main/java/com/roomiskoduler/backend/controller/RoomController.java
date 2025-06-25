package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.dao.RequestInboxDAO;
import com.roomiskoduler.backend.model.*;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.service.SequentialSearch;

import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @GetMapping
    public List<Room> getAllRooms() {
        return RoomFinderDAO.getAllUnoccupiedRooms().toList();
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

    @PostMapping("/available-rooms")
    public List<String> getAvailableLectureRooms(@RequestBody RoomRequestPayload payload) {
        System.out.println("Received payload:");
        System.out.println("Date: " + payload.getDate());
        System.out.println("Time: " + payload.getTime());
        System.out.println("Duration: " + payload.getDuration());

        RoomRequest request = new RoomRequest();
        request.setRoomType("lecture");

        try {
            request.setBookingDate(Date.valueOf(payload.getDate()));
            String formattedTime = payload.getTime().length() == 5 ? payload.getTime() + ":00" : payload.getTime();
            request.setStartTime(Time.valueOf(formattedTime));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid date or time format. Expected formats: yyyy-MM-dd for date, HH:mm:ss or HH:mm for time.");
        }

        request.setDurationMinutes(payload.getDuration());

        RoomLinkedList allRooms = RoomFinderDAO.getAllUnoccupiedRooms();
        RoomLinkedList matches = SequentialSearch.filterMatchingRooms(allRooms, request);

        return matches.toRoomNames();
    }

    @PostMapping("/book-room")
    public Map<String, String> bookRoom(@RequestBody RoomBookingPayload payload) {
        Map<String, String> response = new HashMap<>();

        try {
            boolean success = false;

            if ("admin".equalsIgnoreCase(payload.getRole())) {
                success = RoomFinderDAO.bookRoomDirectly(payload.getRoom(), payload.getDate(), payload.getTime(), payload.getDuration());
                if (success) {
                    response.put("message", "Room booked successfully.");
                } else {
                    response.put("error", "Booking failed (maybe due to conflict).");
                }

            } else if ("faculty".equalsIgnoreCase(payload.getRole())) {

                // Build RoomRequest object
                RoomRequest request = new RoomRequest();
                request.setChosenRoom(payload.getRoom());
                request.setBookingDate(Date.valueOf(payload.getDate()));

                String formattedTime = payload.getTime().length() == 5
                        ? payload.getTime() + ":00"
                        : payload.getTime();

                request.setStartTime(Time.valueOf(formattedTime));
                request.setDurationMinutes(payload.getDuration());

                request.setUserId(0);

                RequestInboxDAO.addToInbox(request);
                response.put("message", "Booking request sent to admin for approval.");
                success = true;

            } else {
                response.put("error", "Invalid user role.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error.");
        }

        return response;
    }


    // Inner class for room status
    public static class RoomStatusResponse {
        private String roomName;
        private String roomType;
        private boolean occupied;
        private String statusText;

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
