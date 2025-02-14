package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.UserDTO;
import com.bodimTikka.bodimTikka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // TODO: advanced search
    @GetMapping("/{name}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String name) {
        Optional<UserDTO> user = userService.getUserByName(name);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        Optional<UserDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Principal principal) {
        // FIXME: additional security?
        userService.deleteUser(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
