package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.dto.AddUserRequestDTO;
import com.bodimTikka.bodimTikka.dto.RoomDTO;
import com.bodimTikka.bodimTikka.dto.UserDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    // TODO: is this really needed
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID id) {
        Optional<Room> room = roomService.getRoomById(id);
        return room.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TODO: add users to room
    @GetMapping("/{roomId}/users")
    public List<UserDTO> getRoomUsers(@PathVariable UUID roomId) {
        return roomService.getRoomUsers(roomId);
    }

    @PostMapping("/{roomId}/users")
    public ResponseEntity<UserInRoom> addUsersToRoom(@PathVariable UUID roomId, @RequestBody AddUserRequestDTO request, Principal principal){
        UserInRoom userInRoom = roomService.createUserInRoom(roomId, request, principal.getName());
        return ResponseEntity.ok(userInRoom);
    }

    @GetMapping("/roomer/{userId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByUserId(@PathVariable UUID userId) {
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
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id, Principal principal) {
        roomService.deleteRoom(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roomer")
    public ResponseEntity<Void> deleteUserFromRoom(@RequestParam UUID uirId, @RequestParam UUID roomId ) {
        roomService.deleteUserFromRoom(uirId, roomId );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/AddAccount")
    public ResponseEntity<?> addAccountToMember(@RequestParam String email, @RequestParam UUID uirId ) {
        roomService.addAccountToMember(email , uirId );
        return ResponseEntity.ok().build();
    }


}
