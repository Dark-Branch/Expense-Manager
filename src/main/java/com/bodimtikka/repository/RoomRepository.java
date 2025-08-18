package com.bodimtikka.repository;

import com.bodimtikka.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Query("SELECT r FROM Room r JOIN UserInRoom ur ON r.id = ur.room.id WHERE ur.user.id = :userId AND ur.isStillAMember = TRUE")
    List<Room> findRoomsByUserId(@Param("userId") UUID userId);
}
