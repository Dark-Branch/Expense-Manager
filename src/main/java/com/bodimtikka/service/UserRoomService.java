package com.bodimtikka.service;

import com.bodimtikka.dto.RoomDTO;
import com.bodimtikka.dto.userroom.UserRoomDTO;
import com.bodimtikka.exception.MemberAlreadyExistsException;
import com.bodimtikka.exception.MemberAlreadyRemovedException;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.User;
import com.bodimtikka.model.UserRoom;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.repository.UserRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserRoomService {

    private final UserRoomRepository userRoomRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * Add a participant to a room
     */
    public UserRoomDTO addMemberToRoom(Long requesterId, Long roomId, Long userId, String nickname) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Check requester is a member
        boolean isMember = userRoomRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(roomId, requesterId);
        if (!isMember) {
            throw new AccessDeniedException("You must be a member of the room to add others");
        }

        // Check duplicate
        if (userId != null) {
            boolean exists = userRoomRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(roomId, userId);
            if (exists) {
                throw new MemberAlreadyExistsException("User is already a member of this room");
            }
        } else {
            boolean nicknameExists = userRoomRepository.existsByRoomIdAndNicknameAndIsStillAMemberTrue(roomId, nickname);
            if (nicknameExists) {
                throw new MemberAlreadyExistsException("A member with this nickname already exists in the room");
            }
        }

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        UserRoom member = new UserRoom(user, room, nickname);
        userRoomRepository.save(member);

        return mapToDTO(member);
    }

    /**
     * Remove a participant from a room (soft delete)
     */
    public void removeParticipantFromRoom(Long userRoomId) {
        userRoomRepository.findById(userRoomId).ifPresent(userRoom -> {
            userRoom.setStillAMember(false);
            userRoomRepository.save(userRoom);
        });
    }

    public List<UserRoomDTO> getRoomMemberships(Long roomId) {
        return userRoomRepository.findUserRoomsByRoomId(roomId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * List all rooms a participant is part of
     */
    public List<UserRoomDTO> getRoomsForParticipant(Long participantId) {
        return userRoomRepository.findByUserId(participantId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Mapping Helper ---
    private UserRoomDTO mapToDTO(UserRoom userRoom) {
        User user = userRoom.getUser();
        Room room = userRoom.getRoom();

        return new UserRoomDTO(
                user == null ? null : user.getId(),
                new RoomDTO(room.getId(), room.getName()),
                userRoom.getNickname()
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
        UserRoom member = userRoomRepository.findById(memberId)
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
        userRoomRepository.save(member);
    }
}
