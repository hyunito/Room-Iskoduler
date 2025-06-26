package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.dao.RoomFinderDAO;
import com.roomiskoduler.backend.dao.RequestInboxDAO;
import com.roomiskoduler.backend.model.*;
import com.roomiskoduler.backend.data.RoomLinkedList;
import com.roomiskoduler.backend.service.SequentialSearch;
import com.roomiskoduler.backend.service.ShellSort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @GetMapping("/scheduled-rooms")
    public ResponseEntity<List<Map<String, Object>>> getScheduledRooms() {
        List<Map<String, Object>> bookings = RequestInboxDAO.getAllCurrentBookings();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/book-rooms")
    public ResponseEntity<?> bookRoom(@RequestBody RoomRequest roomBooked) {
        boolean overlap = RequestInboxDAO.isOverlappingBooking(roomBooked);
        if (overlap) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Room is already booked during this period."));
        }

        boolean success = RequestInboxDAO.bookRoomDirectly(roomBooked);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Room successfully booked", "room", roomBooked.getChosenRoom()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save booking."));
        }
    }


    @PostMapping("/reject-request")
    public ResponseEntity<Map<String, String>> rejectRequest(@RequestBody Map<String, Object> payload) {
        int requestId = (int) payload.get("requestId");
        int adminId = (int) payload.get("adminId");

        boolean success = RequestInboxDAO.deleteRequest(requestId);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Request rejected."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to reject request."));
        }
    }


    @PostMapping("/approve-request")
    public ResponseEntity<Map<String, String>> approveRequest(@RequestBody Map<String, Object> payload) {
        Integer requestId = (Integer) payload.get("requestId");
        Integer adminId = (Integer) payload.get("adminId");

        if (requestId == null || adminId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing requestId or adminId"));
        }

        RoomRequest request = RequestInboxDAO.getRequestById(requestId);
        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Request not found"));
        }

        boolean success = RequestInboxDAO.approveAndBook(request);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Request approved."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to approve request."));
        }
    }

    @GetMapping("/pending-requests")
    public List<Map<String, Object>> getPendingRequests() {
        List<RoomRequest> pending = RequestInboxDAO.getAllPending();

        return pending.stream().map(request -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", request.getUserId());
            map.put("id", request.getRequestId());
            map.put("roomName", request.getChosenRoom());
            map.put("date", request.getBookingDate().toString());
            map.put("time", request.getStartTime().toString());
            map.put("duration", request.getDurationMinutes());
            map.put("students", request.getNumberOfStudents());
            map.put("pcs", request.getRequiredPCs());
            map.put("roomType", RoomFinderDAO.getRoomType(request.getChosenRoom()));

            map.put("userName", RoomFinderDAO.getUserNameById(request.getUserId()));

            return map;
        }).collect(Collectors.toList());
    }


    @PostMapping("/available-rooms")
    public List<?> getAvailableRooms(@RequestBody RoomRequestPayload payload) {
        RoomRequest request = new RoomRequest();

        // Set request properties from payload
        request.setRoomType(payload.getRoomType());

        try {
            String formattedTime = payload.getTime().length() == 5 ? payload.getTime() + ":00" : payload.getTime();
            request.setBookingDate(Date.valueOf(payload.getDate()));
            request.setStartTime(Time.valueOf(formattedTime));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid date or time format.");
        }

        request.setDurationMinutes(payload.getDuration());
        request.setRequiredPCs(payload.getPcs());
        request.setNumberOfStudents(payload.getStudents());

        RoomLinkedList allUnoccupiedRooms = RoomFinderDAO.getAllUnoccupiedRooms();

        RoomLinkedList typeFilteredRooms = new RoomLinkedList();
        for (Room room : allUnoccupiedRooms.toArray()) {
            if ("laboratory".equalsIgnoreCase(request.getRoomType()) && room instanceof LaboratoryRoom) {
                typeFilteredRooms.add(room);
            } else if ("lecture".equalsIgnoreCase(request.getRoomType()) && !(room instanceof LaboratoryRoom)) {
                typeFilteredRooms.add(room);
            }
        }

        RoomLinkedList available = SequentialSearch.filterMatchingRooms(typeFilteredRooms, request);

        if ("laboratory".equalsIgnoreCase(request.getRoomType())) {
            Room[] roomArray = available.toArray();
            ShellSort.sort(roomArray);
            return Arrays.asList(roomArray);
        }

        return available.toRoomNames();
    }



    @GetMapping("/bookings/faculty/{userId}")
    public List<BookingDTO> getFacultyInbox(@PathVariable int userId) {
        List<RoomRequest> requests = RequestInboxDAO.getUserInbox(userId);
        List<BookingDTO> dtos = new ArrayList<>();

        for (RoomRequest req : requests) {
            String room = req.getChosenRoom();
            String date = req.getBookingDate().toString();
            String timeIn = req.getStartTime().toString();
            String timeOut = req.calculateEndTime().toString();
            String status = req.getStatus();

            String roomType = RoomFinderDAO.getRoomType(room);

            dtos.add(new BookingDTO(room, roomType, date, timeIn, timeOut, status));
        }


        return dtos;
    }


    @GetMapping
    public List<Room> getAllRooms() {
        return RoomFinderDAO.getAllUnoccupiedRooms().toList();
    }

    @GetMapping("/status/{roomName}")
    public RoomStatusResponse getRoomStatus(@PathVariable String roomName) {
        boolean occupied = RoomFinderDAO.isRoomCurrentlyOccupied(roomName);
        String rawType = RoomFinderDAO.getRoomType(roomName);

        Map<String, String> specialNames = new HashMap<>();
        specialNames.put("S507", "CCMIT Server Room");
        specialNames.put("N500", "College of Accountancy Faculty Room");
        specialNames.put("S516", "College of Science Accreditation Center");
        specialNames.put("S514", "College of Science Faculty Room");
        specialNames.put("S506", "Curriculum Planning and Development Office");
        specialNames.put("E500", "JPIA Office");
        specialNames.put("S512A", "Sci-Tech Research and Development Center");

        String type;
        String statusText = "";

        if (specialNames.containsKey(roomName)) {
            type = specialNames.get(roomName);
        } else if (rawType != null && !rawType.isEmpty()) {
            type = rawType.substring(0, 1).toUpperCase() + rawType.substring(1).toLowerCase() + " Room";
            statusText = occupied ? "Currently Occupied" : "Available";
        } else {
            type = "Unknown Room";
        }

        RoomStatusResponse response = new RoomStatusResponse();
        response.setRoomName(roomName);
        response.setRoomType(type);
        response.setOccupied(occupied);
        response.setStatusText(statusText);

        return response;
    }


    @PostMapping("/book-room")
    public Map<String, String> bookRoom(@RequestBody RoomBookingPayload payload) {
        Map<String, String> response = new HashMap<>();

        try {
            boolean success = false;

            if ("admin".equalsIgnoreCase(payload.getRole())) {
                success = RoomFinderDAO.bookRoomDirectly(payload.getUserId(), payload.getRoom(), payload.getDate(), payload.getTime(), payload.getDuration());
                if (success) {
                    response.put("message", "Room booked successfully.");
                } else {
                    response.put("error", "Booking failed.");
                }

            } else if ("faculty".equalsIgnoreCase(payload.getRole())) {
                RoomRequest request = new RoomRequest();
                request.setUserId(payload.getUserId());
                request.setChosenRoom(payload.getRoom());
                request.setBookingDate(Date.valueOf(payload.getDate()));
                String formattedTime = payload.getTime().length() == 5 ? payload.getTime() + ":00" : payload.getTime();
                request.setStartTime(Time.valueOf(formattedTime));
                request.setDurationMinutes(payload.getDuration());

                if (RequestInboxDAO.isOverlappingBooking(request)) {
                    response.put("error", "Time conflict with existing booking.");
                } else {
                    RequestInboxDAO.addToInbox(request);
                    response.put("message", "Booking request sent to admin for approval.");
                    success = true;
                }
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
