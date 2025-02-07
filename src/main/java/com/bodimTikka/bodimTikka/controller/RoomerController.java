package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.service.RoomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roomers")
public class RoomerController {

    @Autowired
    private RoomerService roomerService;

    @GetMapping
    public List<User> getAllRoomers() {
        return roomerService.getAllRoomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getRoomerById(@PathVariable Long id) {
        return roomerService.getRoomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createRoomer(@RequestBody User user) {
        return roomerService.saveRoomer(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateRoomer(@PathVariable Long id, @RequestBody User userDetails) {
        return roomerService.getRoomerById(id)
                .map(roomer -> {
                    roomer.setName(userDetails.getName());
                    User updatedUser = roomerService.saveRoomer(roomer);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomer(@PathVariable Long id) {
        roomerService.deleteRoomer(id);
        return ResponseEntity.noContent().build();
    }
}