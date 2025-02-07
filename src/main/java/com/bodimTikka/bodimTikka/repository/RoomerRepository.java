package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomerRepository extends JpaRepository<User, Long> {
}