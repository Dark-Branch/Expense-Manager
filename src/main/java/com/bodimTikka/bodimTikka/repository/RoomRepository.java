package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r JOIN UserInRoom ur ON r.id = ur.room.id WHERE ur.user.id = :userId AND ur.isStillAMember = TRUE")
    List<Room> findRoomsByUserId(@Param("userId") Long userId);
}
