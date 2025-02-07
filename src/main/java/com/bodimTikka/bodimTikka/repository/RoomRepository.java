package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
}
