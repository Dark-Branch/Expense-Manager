package com.bodimtikka.service;

import com.bodimtikka.dto.UserDTO;
import com.bodimtikka.exceptions.NotFoundException;
import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserRepository;
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

    public User getUserObjByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with given E-mail"));

    }
}
