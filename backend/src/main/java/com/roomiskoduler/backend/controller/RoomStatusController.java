/* package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.model.RoomStatusResponse;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomStatusController {

    @GetMapping("/status/{roomName}")
    public RoomStatusResponse getRoomStatus(@PathVariable String roomName) {
        // Example logic, replace with actual DB check
        RoomStatusResponse response = new RoomStatusResponse();
        response.setRoomName(roomName);

        // Set room type based on naming convention
        if (roomName.startsWith("S")) {
            response.setRoomType("Lab Room");
        } else if (roomName.startsWith("N") || roomName.startsWith("E") || roomName.startsWith("W")) {
            response.setRoomType("Lecture Room");
        } else {
            response.setRoomType("Unknown");
        }

        // TODO: Replace this logic with DB call to check if the room is occupied
        boolean isOccupied = checkIfRoomIsOccupied(roomName); // you'll write this
        response.setOccupied(isOccupied);
        response.setStatusText(isOccupied ? "Currently Occupied" : "Available");

        return response;
    }

    // Temporary stub — replace with actual query from your database
    private boolean checkIfRoomIsOccupied(String roomName) {
        // Example: fake room status
        return roomName.equalsIgnoreCase("S504") || roomName.equalsIgnoreCase("W502");
    }
}
*/