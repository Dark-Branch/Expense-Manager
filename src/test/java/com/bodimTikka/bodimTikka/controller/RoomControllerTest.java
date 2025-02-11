package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.AddUserRequestDTO;
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
        userRepository.deleteAll();
        roomRepository.deleteAll();
        userInRoomRepository.deleteAll();

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
        userInRoom1.setName("yow");
        //TODO: hide is still a member part
        userInRoom1.setStillAMember(true);
        userInRoom1 = userInRoomRepository.save(userInRoom1);

        UserInRoom userInRoom2 = new UserInRoom();
        userInRoom2.setRoom(testRoom2);
        userInRoom2.setUser(user);
        userInRoom2.setName("yo");
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

    // TODO: these all will change when we use principal object
    // TODO: when adding those, write tests for various cases of that too
    @Test
    void testAddUserToRoom_ValidRegisteredUser() {
        User user1 = new User("John");
        User user2 = new User("John");
        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");

        user1 = userRepository.save(user1);
        userInRoomRepository.save(userInRoom1);
        user2 = userRepository.save(user2);

        AddUserRequestDTO request = new AddUserRequestDTO(user2.getId(), "John", true);
        ResponseEntity<UserInRoom> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, UserInRoom.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom userInRoom = response.getBody();
        assertThat(userInRoom).isNotNull();
        assertThat(userInRoom.getUser().getId()).isEqualTo(user2.getId());
        assertThat(userInRoom.getName()).isEqualTo("John");
        assertThat(userInRoom.isRegistered()).isTrue();
    }

    @Test
    void testAddUserToRoom_ValidUnregisteredUser() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);
        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(null, "Guest", false);
        ResponseEntity<UserInRoom> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users"+ "?senderId=" + user1.getId(), request, UserInRoom.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom userInRoom = response.getBody();
        assertThat(userInRoom).isNotNull();
        assertThat(userInRoom.getUser()).isNull();
        assertThat(userInRoom.getName()).isEqualTo("Guest");
        assertThat(userInRoom.isRegistered()).isFalse();
    }

    @Test
    void testAddUserToRoom_MissingName() {
        // TODO: these test setup is redundant in almost every case; refactor
        User user1 = new User("John");
        user1 = userRepository.save(user1);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        User user2 = new User("John");
        user2 = userRepository.save(user2);

        AddUserRequestDTO request = new AddUserRequestDTO(user2.getId(), "", true);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Name cannot be empty");
    }

    // TODO: i feel is registered can be managed internally, at least for registered ones
    @Test
    void testAddUserToRoom_MissingUserIdForRegisteredUser() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(null, "John", true);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User ID is required for registered users");
    }

    @Test
    void testAddUserToRoom_UserIdProvidedForUnregisteredUser() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        User user = new User("GuestUser");
        user = userRepository.save(user);

        AddUserRequestDTO request = new AddUserRequestDTO(user.getId(), "Guest", false);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User ID must be null for unregistered users");
    }

    @Test
    void testAddUserToRoom_UserAlreadyInRoom() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);
        User user2 = new User("John");
        user2 = userRepository.save(user2);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(user2.getId(), "John", true);
        ResponseEntity<UserInRoom> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, UserInRoom.class);

        // Attempting to add the same user again should fail
        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, String.class);
        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateResponse.getBody()).contains("User is already in the room");
    }

    @Test
    void testAddUserToRoom_InvalidUserId() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);
        User user2 = new User("John");
        user2 = userRepository.save(user2);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(999L, "John", true);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users" + "?senderId=" + user1.getId(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User with ID 999 does not exist");
    }

    @Test
    void testAddUserToRoom_InvalidRoomId() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);
        User user2 = new User("John");
        user2 = userRepository.save(user2);

        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(user2.getId(), "John", true);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/rooms/999/users"  + "?senderId=" + user1.getId(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}