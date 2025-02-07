package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.model.Roomer;
import com.bodimTikka.bodimTikka.repository.RoomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomerService {

    @Autowired
    private RoomerRepository roomerRepository;

    public List<Roomer> getAllRoomers() {
        return roomerRepository.findAll();
    }

    public Optional<Roomer> getRoomerById(Long id) {
        return roomerRepository.findById(id);
    }

    public Roomer saveRoomer(Roomer roomer) {
        return roomerRepository.save(roomer);
    }

    public void deleteRoomer(Long id) {
        roomerRepository.deleteById(id);
    }
}