package com.bodimtikka.repository;

import com.bodimtikka.model.UserInRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInRoomRepository extends JpaRepository<UserInRoom, UUID> {

    @Query("SELECT uir FROM UserInRoom uir WHERE uir.room.id = :roomId AND uir.isStillAMember = true")
    List<UserInRoom> findUsersByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT uir.id FROM UserInRoom uir WHERE uir.room.id = :roomId")
    List<UUID> findUserIdsByRoomId(@Param("roomId") UUID roomId);

    Boolean existsByUserIdAndRoomIdAndIsStillAMember(UUID userId, UUID roomId, Boolean isStillAMember);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_in_room (user_id, room_id, name) VALUES (:userId, :roomId, :name)", nativeQuery = true)
    void addUserToRoom(@Param("userId") UUID userId, @Param("roomId") UUID roomId, @Param("name") String name);

    @Modifying
    @Transactional
    @Query("UPDATE UserInRoom uir SET uir.isAdmin = TRUE, uir.isRegistered = TRUE WHERE uir.user.id = :userId AND uir.room.id = :roomId")
    void addAdminUser(@Param("userId") UUID userId, @Param("roomId") UUID roomId);

    @Query("SELECT uir.isAdmin FROM UserInRoom uir WHERE uir.user.id = :userId AND uir.room.id = :roomId")
    Boolean isUserAdmin(@Param("userId") UUID userId, @Param("roomId") UUID roomId);



    @Query("SELECT COUNT(uir) > 0 FROM UserInRoom uir WHERE uir.id = :uirId AND uir.room.id = :roomId AND uir.isStillAMember = :isStillAMember")
    boolean existsByUserInRoomIdAndRoomIdAndIsStillAMember(@Param("uirId") UUID uirId, @Param("roomId") UUID roomId, @Param("isStillAMember") boolean isStillAMember);

    @Query("SELECT uir FROM UserInRoom uir WHERE uir.id = :uirId AND uir.room.id = :roomId")
    Optional<UserInRoom> findByUirIdAndRoomId(@Param("uirId") UUID uirId, @Param("roomId") UUID roomId);
}
