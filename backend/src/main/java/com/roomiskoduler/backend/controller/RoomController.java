package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.model.Room;
import com.roomiskoduler.backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*") // Allow requests from frontend
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{roomName}")
    public Room getRoomStatus(@PathVariable String roomName) {
        return roomService.getRoomByName(roomName);
    }
}
