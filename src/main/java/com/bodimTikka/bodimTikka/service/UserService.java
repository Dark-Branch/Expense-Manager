package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.dto.UserDTO;
import com.bodimTikka.bodimTikka.exceptions.NotFoundException;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserDTO> getUserByName(String name) {
        return userRepository.findUserProjectionByName(name);
    }

    public UserDTO getUserByEmail(String email) {
        return userRepository.findUserProjectionByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with given E-mail"));
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String email) {
        userRepository.removeByEmail(email);
    }

    public boolean existsById(UUID userId){
        return userRepository.existsById(userId);
    }

    public Optional<UserDTO> findUserProjectionByEmail(String email) {
        return userRepository.findUserProjectionByEmail(email);
    }
}
