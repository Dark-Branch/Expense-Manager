package com.bodimtikka.repository;

import com.bodimtikka.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    // Find participants not yet linked to a user
    List<Participant> findByUserIsNull();

    // Find all participants linked to a specific user
    List<Participant> findByUserId(Long userId);

    // Optional: find by display name (useful for searches)
    List<Participant> findByDisplayNameContainingIgnoreCase(String keyword);
}
