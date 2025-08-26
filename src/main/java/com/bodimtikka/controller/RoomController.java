package com.bodimtikka.controller;

import com.bodimtikka.model.Room;
import com.bodimtikka.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // --- Create a room ---
    @PostMapping("/create")
    public Room createRoom(@RequestBody Room room) {
        return roomService.saveRoom(room);
    }

    // --- Get room by ID ---
    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    // --- List all rooms ---
    @GetMapping("/all")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    // --- Delete a room ---
    @DeleteMapping("/delete/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }

    // Optional: find room by name
    @GetMapping("/name/{name}")
    public Room getRoomByName(@PathVariable String name) {
        return roomService.getRoomByName(name);
    }
}
