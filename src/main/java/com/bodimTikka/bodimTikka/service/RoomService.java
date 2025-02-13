package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.AddUserRequestDTO;
import com.bodimTikka.bodimTikka.DTO.RoomDTO;
import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.DTO.UserProjection;
import com.bodimTikka.bodimTikka.exceptions.InvalidRequestException;
import com.bodimTikka.bodimTikka.exceptions.NotFoundException;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserInRoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserInRoomRepository userInRoomRepository;
    @Autowired
    private UserService userService;

    // TODO: handle null case?
    public List<UserDTO> getRoomUsers(Long roomId) {
        List<UserInRoom> usersInRoom = userInRoomRepository.findUsersByRoomId(roomId);
        return usersInRoom.stream()
                .map(uir -> new UserDTO(uir.getUser().getId(), uir.getUser().getName(), uir.getUser().getEmail()))
                .collect(Collectors.toList());
    }

    public List<Long> getRoomUserIDs(Long roomID){
        return userInRoomRepository.findUserIdsByRoomId(roomID);
    }

    // TODO: add main room mambers in get rooms dto and then can lazily load members for other rooms
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public List<RoomDTO> getRoomsByUserId(Long userId) {
        List<Room> rooms = roomRepository.findRoomsByUserId(userId);
        return rooms.stream()
                .map(room -> new RoomDTO(room.getId(), room.getName()))
                .collect(Collectors.toList());
    }

    public UserInRoom createUserInRoom(Long senderId, Long roomId, AddUserRequestDTO request){
        // TODO: maybe checking for if user have the wanted role
        String name = request.getName();
        boolean isRegistered = request.getIsRegistered();
        Long userId = request.getUserId();

        verify(senderId, roomId, name, isRegistered, userId);

        UserInRoom userInRoom = new UserInRoom();
        userInRoom.setUser(userId != null ? new User(userId) : null);
        userInRoom.setRoom(new Room(roomId));
        userInRoom.setName(name);
        userInRoom.setRegistered(isRegistered);
        userInRoom = userInRoomRepository.save(userInRoom);
        System.out.println(userInRoom.getName());
        return userInRoom;
    }

    private void verify(Long senderId, Long roomId, String name, boolean isRegistered, Long userId) {
        if (!userInRoomRepository.existsByUserIdAndRoomId(senderId, roomId)){
            throw new InvalidRequestException("Current user doesn't belong to the room");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Name cannot be empty");
        }

        if (isRegistered) {
            if (userId == null) {
                throw new InvalidRequestException("User ID is required for registered users");
            }
            if (!userService.existsById(userId)) {
                throw new InvalidRequestException("User with ID " + userId + " does not exist");
            }
        } else {
            if (userId != null) {
                throw new InvalidRequestException("User ID must be null for unregistered users");
            }
        }

        if (isRegistered && userInRoomRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new InvalidRequestException("User is already in the room");
        }
    }

    public Room createRoomForUser(Room room, String email) {
        UserProjection userProjection = userService.findUserProjectionByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new InvalidRequestException("Room name cannot be null or empty.");
        }

        Room createdRoom = roomRepository.save(room);
        Long userId = userProjection.getId();
        Long roomId = createdRoom.getId();
        userInRoomRepository.addUserToRoom(userId, roomId, userProjection.getName());
        userInRoomRepository.addAdminUser(userId, roomId);

        return createdRoom;
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public boolean existsById(Long roomId) {
        return roomRepository.existsById(roomId);
    }
}
