package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.model.Roomer;
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
    public List<Roomer> getAllRoomers() {
        return roomerService.getAllRoomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Roomer> getRoomerById(@PathVariable Long id) {
        return roomerService.getRoomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Roomer createRoomer(@RequestBody Roomer roomer) {
        return roomerService.saveRoomer(roomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Roomer> updateRoomer(@PathVariable Long id, @RequestBody Roomer roomerDetails) {
        return roomerService.getRoomerById(id)
                .map(roomer -> {
                    roomer.setName(roomerDetails.getName());
                    Roomer updatedRoomer = roomerService.saveRoomer(roomer);
                    return ResponseEntity.ok(updatedRoomer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomer(@PathVariable Long id) {
        roomerService.deleteRoomer(id);
        return ResponseEntity.noContent().build();
    }
}