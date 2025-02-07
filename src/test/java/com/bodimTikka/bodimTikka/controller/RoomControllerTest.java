package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setName("Test Room");

        // Create test room
        ResponseEntity<Room> response = restTemplate.postForEntity("/rooms", testRoom, Room.class);
        testRoom = response.getBody();
    }

    @Test
    void testGetRoomById() {
        ResponseEntity<Room> response = restTemplate.getForEntity("/rooms/" + testRoom.getId(), Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Room");
    }

    @Test
    void testGetRoomByInvalidId() {
        ResponseEntity<Room> response = restTemplate.getForEntity("/rooms/99999", Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateRoom() {
        Room newRoom = new Room();
        newRoom.setName("New Test Room");
        ResponseEntity<Room> response = restTemplate.postForEntity("/rooms", newRoom, Room.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("New Test Room");
    }

    @Test
    void testDeleteRoom() {
        ResponseEntity<Void> response = restTemplate.exchange("/rooms/" + testRoom.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testGetRoomUsers() {
        ResponseEntity<List> response = restTemplate.getForEntity("/rooms/" + testRoom.getId() + "/users", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
