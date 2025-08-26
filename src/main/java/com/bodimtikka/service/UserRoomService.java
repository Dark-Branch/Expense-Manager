package com.bodimtikka.service;

import com.bodimtikka.dto.ParticipantDTO;
import com.bodimtikka.dto.RoomDTO;
import com.bodimtikka.dto.UserRoomDTO;
import com.bodimtikka.model.Participant;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.UserRoom;
import com.bodimtikka.repository.ParticipantRepository;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoomService {

    private final UserRoomRepository userRoomRepository;
    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;

    public UserRoomService(UserRoomRepository userRoomRepository, ParticipantRepository participantRepository, RoomRepository roomRepository) {
        this.userRoomRepository = userRoomRepository;
        this.participantRepository = participantRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Add a participant to a room
     */
    public UserRoomDTO addParticipantToRoom(Long participantId, Long roomId, String nickname) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        UserRoom userRoom = new UserRoom(participant, room, nickname);
        userRoom.setStillAMember(true);
        userRoom = userRoomRepository.save(userRoom);

        return mapToDTO(userRoom);
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
        return userRoomRepository.findByParticipantId(participantId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Mapping Helper ---
    private UserRoomDTO mapToDTO(UserRoom userRoom) {
        Participant participant = userRoom.getParticipant();
        Room room = userRoom.getRoom();

        return new UserRoomDTO(
                userRoom.getId(),
                new ParticipantDTO(participant.getId(), participant.getDisplayName()),
                new RoomDTO(room.getId(), room.getName()),
                userRoom.getNickname(),
                userRoom.isStillAMember(),
                userRoom.getJoinedAt()
        );
    }
}
