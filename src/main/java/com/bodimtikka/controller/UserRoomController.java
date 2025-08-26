package com.bodimtikka.controller;

import com.bodimtikka.dto.UserRoomDTO;
import com.bodimtikka.service.UserRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-rooms")
public class UserRoomController {

    private final UserRoomService userRoomService;

    public UserRoomController(UserRoomService userRoomService) {
        this.userRoomService = userRoomService;
    }

    /**
     * Add a participant to a room
     */
    @PostMapping("/add")
    public ResponseEntity<UserRoomDTO> addParticipantToRoom(
            @RequestParam Long participantId,
            @RequestParam Long roomId,
            @RequestParam String nickname) {

        UserRoomDTO userRoomDTO = userRoomService.addParticipantToRoom(participantId, roomId, nickname);
        return ResponseEntity.ok(userRoomDTO);
    }

    /**
     * Remove participant (soft delete)
     */
    @DeleteMapping("/{userRoomId}")
    public ResponseEntity<Void> removeParticipantFromRoom(@PathVariable Long userRoomId) {
        userRoomService.removeParticipantFromRoom(userRoomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get memberships for a room
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<UserRoomDTO>> getRoomMemberships(@PathVariable Long roomId) {
        List<UserRoomDTO> memberships = userRoomService.getRoomMemberships(roomId);
        return ResponseEntity.ok(memberships);
    }

    /**
     * Get rooms for a participant
     */
    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<UserRoomDTO>> getRoomsForParticipant(@PathVariable Long participantId) {
        List<UserRoomDTO> rooms = userRoomService.getRoomsForParticipant(participantId);
        return ResponseEntity.ok(rooms);
    }
}
