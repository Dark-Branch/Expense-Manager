package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.RoomDTO;
import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserInRoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserInRoomRepository userInRoomRepository;

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

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}
