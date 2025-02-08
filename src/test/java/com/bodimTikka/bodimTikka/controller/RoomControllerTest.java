package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.RoomDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserInRoomRepository;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInRoomRepository userInRoomRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room testRoom;
    private User user;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setName("Test Room");

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

    @Test
    void testGetRoomsByUserId_ValidRoomerId() {
        User user = new User("user");
        user = userRepository.save(user);

        Room testRoom1 = new Room("Test Room 1");
        Room testRoom2 = new Room("Test Room 2");
        testRoom1 = roomRepository.save(testRoom1);
        testRoom2 = roomRepository.save(testRoom2);

        UserInRoom userInRoom1 = new UserInRoom();
        userInRoom1.setRoom(testRoom1);
        userInRoom1.setUser(user);
        //TODO: hide is still a member part
        userInRoom1.setStillAMember(true);
        userInRoom1 = userInRoomRepository.save(userInRoom1);

        UserInRoom userInRoom2 = new UserInRoom();
        userInRoom2.setRoom(testRoom2);
        userInRoom2.setUser(user);
        userInRoom2.setStillAMember(true);
        userInRoom2 = userInRoomRepository.save(userInRoom2);

        ResponseEntity<List<RoomDTO>> response = restTemplate.exchange(
                "/rooms/roomer/" + user.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Test Room 1");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Test Room 2");
    }


    @Test
    void testGetRoomByRoomerId_InvalidRoomerId() {
        ResponseEntity<Room> response = restTemplate.getForEntity("/rooms/roomer/99999", Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetRoomByRoomerId_EmptyRoomerId() {
        ResponseEntity<Room> response = restTemplate.getForEntity("/rooms/roomer/", Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetRoomByRoomerId_BadRequest() {
        ResponseEntity<Room> response = restTemplate.getForEntity("/rooms/roomer/error", Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}