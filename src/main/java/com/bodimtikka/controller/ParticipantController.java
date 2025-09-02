package com.bodimtikka.controller;

import com.bodimtikka.dto.participant.ParticipantDTO;
import com.bodimtikka.dto.participant.AddParticipantRequest;
import com.bodimtikka.security.JwtUserPrincipal;
import com.bodimtikka.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-rooms")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * Add a participant to a room
     */
    @PostMapping("/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantDTO> addMemberToRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody AddParticipantRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        System.out.println("jeje");
        ParticipantDTO dto = participantService.addMemberToRoom(
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

        participantService.removeMemberFromRoom(principal.id(), roomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
