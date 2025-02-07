package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.model.Room;
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

    public Optional<Room> getRoomById(String id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }
}
