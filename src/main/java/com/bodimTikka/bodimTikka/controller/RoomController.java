package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.RoomDTO;
import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);
        return room.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TODO: add users to room
    @GetMapping("/{roomId}/users")
    public List<UserDTO> getRoomUsers(@PathVariable Long roomId) {
        return roomService.getRoomUsers(roomId);
    }

    @GetMapping("/roomer/{userId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByUserId(@PathVariable Long userId) {
        List<RoomDTO> roomDTOs = roomService.getRoomsByUserId(userId);
        return roomDTOs.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(roomDTOs);
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.saveRoom(room);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
