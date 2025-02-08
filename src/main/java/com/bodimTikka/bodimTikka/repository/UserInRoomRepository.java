package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.UserInRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInRoomRepository extends JpaRepository<UserInRoom, Long> {

    @Query("SELECT uir FROM UserInRoom uir WHERE uir.room.id = :roomId AND uir.isStillAMember = true")
    List<UserInRoom> findUsersByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT uir.user.id FROM UserInRoom uir WHERE uir.room.id = :roomId")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);
}
