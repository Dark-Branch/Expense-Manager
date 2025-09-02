package com.bodimtikka.service;

import com.bodimtikka.dto.RoomDTO;
import com.bodimtikka.dto.participant.ParticipantDTO;
import com.bodimtikka.exception.MemberAlreadyExistsException;
import com.bodimtikka.exception.MemberAlreadyRemovedException;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.User;
import com.bodimtikka.model.Participant;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.repository.ParticipantRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * Add a participant to a room
     */
    public ParticipantDTO addMemberToRoom(Long requesterId, Long roomId, Long userId, String nickname) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Check requester is a member
        boolean isMember = participantRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(roomId, requesterId);
        if (!isMember) {
            throw new AccessDeniedException("You must be a member of the room to add others");
        }

        // Check duplicate
        if (userId != null) {
            boolean exists = participantRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(roomId, userId);
            if (exists) {
                throw new MemberAlreadyExistsException("User is already a member of this room");
            }
        } else {
            boolean nicknameExists = participantRepository.existsByRoomIdAndNicknameAndIsStillAMemberTrue(roomId, nickname);
            if (nicknameExists) {
                throw new MemberAlreadyExistsException("A member with this nickname already exists in the room");
            }
        }

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        Participant member = new Participant(user, room, nickname);
        participantRepository.save(member);

        return mapToDTO(member);
    }

    /**
     * Remove a participant from a room (soft delete)
     */
    public void removeParticipantFromRoom(Long userRoomId) {
        participantRepository.findById(userRoomId).ifPresent(userRoom -> {
            userRoom.setStillAMember(false);
            participantRepository.save(userRoom);
        });
    }

    public List<ParticipantDTO> getRoomMemberships(Long roomId) {
        return participantRepository.findUserRoomsByRoomId(roomId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * List all rooms a participant is part of
     */
    public List<ParticipantDTO> getRoomsForParticipant(Long participantId) {
        return participantRepository.findByUserId(participantId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Mapping Helper ---
    private ParticipantDTO mapToDTO(Participant participant) {
        User user = participant.getUser();
        Room room = participant.getRoom();

        return new ParticipantDTO(
                user == null ? null : user.getId(),
                new RoomDTO(room.getId(), room.getName()),
                participant.getNickname()
        );
    }

    public void removeMemberFromRoom(Long requesterId, Long roomId, Long memberId) {
        // Fetch room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Check requester is the owner
        if (!room.getOwner().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only the owner can remove members");
        }

        // Fetch member
        Participant member = participantRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Check member belongs to this room
        if (!member.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Member does not belong to this room");
        }

        // Soft delete
        if (!member.isStillAMember()) {
            throw new MemberAlreadyRemovedException("Member is already removed");
        }
        member.setStillAMember(false);
        participantRepository.save(member);
    }
}
