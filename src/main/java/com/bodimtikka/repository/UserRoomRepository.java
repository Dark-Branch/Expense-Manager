package com.bodimtikka.repository;

import com.bodimtikka.model.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {

    // Find all UserRooms for a given participant
    List<UserRoom> findByUserId(Long userId);


    @Query("SELECT ur FROM UserRoom ur WHERE ur.room.id = :roomId")
    List<UserRoom> findUserRoomsByRoomId(@Param("roomId") Long roomId);

    // Check if a given user is still an active member of the room
    boolean existsByRoomIdAndUserIdAndIsStillAMemberTrue(Long roomId, Long userId);

    boolean existsByRoomIdAndNicknameAndIsStillAMemberTrue(Long roomId, String nickname);
}
