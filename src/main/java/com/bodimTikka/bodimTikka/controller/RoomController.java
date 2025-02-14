package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.AddUserRequestDTO;
import com.bodimTikka.bodimTikka.DTO.RoomDTO;
import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    // TODO: is this really needed
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

    @PostMapping("/{roomId}/users")
    public ResponseEntity<UserInRoom> addUsersToRoom(@PathVariable Long roomId, @RequestBody AddUserRequestDTO request, Principal principal){
        UserInRoom userInRoom = roomService.createUserInRoom(roomId, request, principal.getName());
        return ResponseEntity.ok(userInRoom);
    }

    @GetMapping("/roomer/{userId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByUserId(@PathVariable Long userId) {
        List<RoomDTO> roomDTOs = roomService.getRoomsByUserId(userId);
        return roomDTOs.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(roomDTOs);
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room room, Principal principal) {
        String email = principal.getName();

        Room createdRoom = roomService.createRoomForUser(room, email);

        return ResponseEntity.ok(createdRoom);
    }

    // TODO: where to delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id, Principal principal) {
        roomService.deleteRoom(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
