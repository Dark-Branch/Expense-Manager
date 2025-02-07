package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.RoomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomerService {

    @Autowired
    private RoomerRepository roomerRepository;

    public List<User> getAllRoomers() {
        return roomerRepository.findAll();
    }

    public Optional<User> getRoomerById(Long id) {
        return roomerRepository.findById(id);
    }

    public User saveRoomer(User user) {
        return roomerRepository.save(user);
    }

    public void deleteRoomer(Long id) {
        roomerRepository.deleteById(id);
    }
}