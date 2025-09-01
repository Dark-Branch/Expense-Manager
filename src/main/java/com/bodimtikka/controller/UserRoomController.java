package com.bodimtikka.controller;

import com.bodimtikka.dto.userroom.UserRoomDTO;
import com.bodimtikka.dto.userroom.AddMemberRequest;
import com.bodimtikka.security.JwtUserPrincipal;
import com.bodimtikka.service.UserRoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserRoomDTO> addMemberToRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        System.out.println("jeje");
        UserRoomDTO dto = userRoomService.addMemberToRoom(
                principal.id(), roomId, request.getUserId(), request.getNickname()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Remove participant (soft delete)
     */
    @DeleteMapping("/{roomId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMemberFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        userRoomService.removeMemberFromRoom(principal.id(), roomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
