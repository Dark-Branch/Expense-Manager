package com.bodimtikka.controller;

import com.bodimtikka.dto.room.RoomRequest;
import com.bodimtikka.dto.room.RoomResponse;
import com.bodimtikka.dto.room.RoomSummaryResponse;
import com.bodimtikka.security.JwtUserPrincipal;
import com.bodimtikka.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Create a new room. The authenticated user becomes the owner.
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid
            @RequestBody RoomRequest request,
            @AuthenticationPrincipal JwtUserPrincipal userDetails) {
        RoomResponse room = roomService.createRoom(request, userDetails.id());
        return ResponseEntity.ok(room);
    }

    /**
     * Get room details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomSummaryResponse> getRoomById(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal userDetails) {
        RoomSummaryResponse response = roomService.getRoomById(id, userDetails.id());
        return ResponseEntity.ok(response);
    }

    /**
     * List all rooms the user is a member of.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // only authenticated users can access
    public ResponseEntity<List<RoomSummaryResponse>> getUserRooms(
            @AuthenticationPrincipal JwtUserPrincipal userDetails) {
        List<RoomSummaryResponse> rooms = roomService.getUserRooms(userDetails.id());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Delete a room. Only the owner can delete.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // only authenticated users
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal userDetails) {
        roomService.deleteRoom(id, userDetails.id());
        return ResponseEntity.noContent().build();
    }
}
