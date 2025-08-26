package com.bodimtikka.repository;


import com.bodimtikka.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find by exact name
    Room findByName(String name);

    // Search by partial name (case-insensitive)
    List<Room> findByNameContainingIgnoreCase(String keyword);

    // Later: Find all rooms a user participates in (through UserRoom)
    // This one typically requires @Query with a join
}
