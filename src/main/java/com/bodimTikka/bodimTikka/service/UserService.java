package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findUserProjectionByEmail(email);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String email) {
        userRepository.removeByEmail(email);
    }

    public boolean existsById(Long userId){
        return userRepository.existsById(userId);
    }

    public Optional<UserDTO> findUserProjectionByEmail(String email) {
        return userRepository.findUserProjectionByEmail(email);
    }
}
