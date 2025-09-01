package com.bodimtikka.service;

import com.bodimtikka.dto.room.*;
import com.bodimtikka.exception.ResourceNotFoundException;
import com.bodimtikka.model.Participant;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.User;
import com.bodimtikka.model.UserRoom;
import com.bodimtikka.repository.ParticipantRepository;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final ParticipantRepository participantRepository;
    private final UserService userService;

    // Create or update a room
    private Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    // Get room by ID
    @Transactional(readOnly = true)
    public RoomSummaryResponse getRoomById(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        // Check if the user is a participant in this room
        boolean isMember = room.getUserRooms().stream()
                .anyMatch(ur -> ur.getParticipant().getUser().getId().equals(userId));

        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this room");
        }

        // Map participants for response (using nickname from UserRoom)
        List<ParticipantSummary> participants = room.getUserRooms().stream()
                .filter(UserRoom::isStillAMember)
                .map(ur -> new ParticipantSummary(
                        ur.getParticipant().getId(),
                        ur.getNickname()
                ))
                .toList();

        return new RoomSummaryResponse(
                room.getId(),
                room.getName(),
                participants
        );
    }

    // Get all rooms
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Delete room
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    // Optional: find by name
    public Room getRoomByName(String name) {
        return roomRepository.findByName(name);
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long userId) {
        // --- Fetch user from DB using ID ---
        User ownerUser = userService.getUserByIdOrThrow(userId);

        // --- Create a Participant for the owner ---
        Participant ownerParticipant = new Participant();
        ownerParticipant.setUser(ownerUser);
        ownerParticipant.setDisplayName(ownerUser.getName()); // initial nickname
        participantRepository.save(ownerParticipant); // save participant first

        // --- Create room ---
        Room room = new Room();
        room.setName(request.getName());
        room.setOwner(ownerUser);

        // --- Assign owner as a UserRoom ---
        UserRoom ownerUserRoom = new UserRoom();
        ownerUserRoom.setParticipant(ownerParticipant);
        ownerUserRoom.setRoom(room);
        ownerUserRoom.setNickname(ownerUser.getName());
        ownerUserRoom.setStillAMember(true);

        // --- Link bidirectionally ---
        room.getUserRooms().add(ownerUserRoom);

        // --- Save room (cascade will save UserRoom) ---
        Room savedRoom = roomRepository.save(room);

        // --- Prepare participants list for response ---
        List<UserSummary> participants = savedRoom.getUserRooms().stream()
                .map(ur -> {
                    Participant p = ur.getParticipant();
                    User u = p.getUser();
                    return new UserSummary(
                            u.getId(),
                            u.getName(),
                            u.getEmail()
                    );
                })
                .toList();

        // --- Build DTO ---
        return new RoomResponse(
                savedRoom.getId(),
                savedRoom.getName(),
                new UserSummary(ownerUser.getId(), ownerUser.getName(), ownerUser.getEmail()),
                participants
        );
    }

    @Transactional(readOnly = true)
    public List<RoomSummaryResponse> getUserRooms(Long userId) {
        // Fetch all UserRoom entries where participant.user.id == userId
        List<UserRoom> userRooms = userRoomRepository.findByParticipantUserId(userId);

        // Map to RoomSummaryResponse DTO
        return userRooms.stream()
                .map(ur -> {
                    Room room = ur.getRoom();
                    List<ParticipantSummary> participants = room.getUserRooms().stream()
                            .filter(UserRoom::isStillAMember)
                            .map(p -> new ParticipantSummary(
                                    p.getParticipant().getId(),
                                    p.getNickname()
                            ))
                            .toList();
                    return new RoomSummaryResponse(room.getId(), room.getName(), participants);
                })
                .toList();
    }

    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        // Fetch the room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        // Check if the requesting user is the owner
        if (!room.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the owner can delete this room");
        }

        // Delete the room
        roomRepository.delete(room);
    }
}
