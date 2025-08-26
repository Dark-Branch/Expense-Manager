package com.bodimtikka.service;

import com.bodimtikka.model.Room;
import com.bodimtikka.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // Create or update a room
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    // Get room by ID
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));
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
}
