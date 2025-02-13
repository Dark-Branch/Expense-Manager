package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.*;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserInRoomRepository;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import com.bodimTikka.bodimTikka.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInRoomRepository userInRoomRepository;

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private AuthService authService;

    private Room testRoom;
    private User user;
    private String token;
    private final String BaseURL = "/api/rooms";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roomRepository.deleteAll();
        userInRoomRepository.deleteAll();

        setupSignedUserAndGetToken();

        testRoom = new Room();
        testRoom.setName("Test Room");

        testRoom = roomRepository.save(testRoom);
    }

    private void setupSignedUserAndGetToken() {
        user = new User();
        user.setName("test_user");
        user.setEmail("example@example.com");
        // need to use in login req
        String password = "password";
        user.setPassword(password);

        SignupRequest request = SignupRequest.builder()
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .build();
        user = authService.registerUser(request);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                LoginRequest.builder()
                        .email(user.getEmail())
                        .password(password)
                        .build(),
                String.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        token = AuthControllerTests.extractToken(loginResponse.getBody());
    }

    private HttpHeaders getHttpHeadersWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Disabled
    @Test
    void testGetRoomById() {
        ResponseEntity<Room> response = restTemplate.getForEntity(BaseURL + testRoom.getId(), Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Room");
    }

    @Disabled
    @Test
    void testGetRoomByInvalidId() {
        ResponseEntity<Room> response = restTemplate.getForEntity(BaseURL + "99999", Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testCreateRoom() {
        Room newRoom = new Room();
        newRoom.setName("New Test Room");

        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Room> entity = new HttpEntity<>(newRoom, headers);

        ResponseEntity<Room> response = restTemplate.exchange(BaseURL, HttpMethod.POST, entity, Room.class);

        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(newRoom.getName());
    }

    // TODO: test without auth
    @Test
    void testDeleteRoom() {
        addUserAndRoomRecordToUserInRoom(testRoom);
        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Room> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(BaseURL + "/" + testRoom.getId(), HttpMethod.DELETE, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private void addUserAndRoomRecordToUserInRoom(Room testRoom) {
        UserInRoom userInRoom = new UserInRoom();
        userInRoom.setRoom(testRoom);
        userInRoom.setUser(user);
        userInRoom.setName("yow");
        userInRoom.setStillAMember(true);
        userInRoom.setAdmin(true);
        userInRoom = userInRoomRepository.save(userInRoom);
    }

    @Disabled("write this")
    @Test
    void testDeleteRoom_UserNotAdmin() {
        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId(),
                HttpMethod.DELETE,
                entity,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("User is not an admin of this room.");
    }

    @Test
    void testGetRoomUsers() {
        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Room> entity = new HttpEntity<>(testRoom, headers);

        ResponseEntity<List> response = restTemplate.exchange(BaseURL + "/" + testRoom.getId() + "/users", HttpMethod.GET, entity, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testGetRoomsByUserId_ValidRoomerId() {
        Room testRoom1 = new Room("Test Room 1");
        Room testRoom2 = new Room("Test Room 2");

        addUserToTwoRoomsUsingRepositories(testRoom1, testRoom2);

        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<RoomDTO>> response = restTemplate.exchange(
                BaseURL + "/roomer/" + user.getId(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RoomDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Test Room 1");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Test Room 2");
    }

    private void addUserToTwoRoomsUsingRepositories(Room testRoom1, Room testRoom2) {
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
    }

    @Test
    void testGetRoomByRoomerId_InvalidRoomerId() {
        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Room> response = restTemplate.exchange(BaseURL + "/roomer/99999", HttpMethod.GET, entity, Room.class);
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

    // TODO: auth here
    @Disabled
    @Test
    void testAssignAdmin_Valid() {
        // Create and add a user to the room
        User user = new User("John");
        user = userRepository.save(user);
        UserInRoom userInRoom = new UserInRoom(user, testRoom, "pakaya");
        userInRoom.setRegistered(true);
        userInRoomRepository.save(userInRoom);

        ResponseEntity<UserInRoom> adminResponse = restTemplate.postForEntity(
                "/rooms/" + testRoom.getId() + "/users/" + user.getId() + "/assignAdmin", null, UserInRoom.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom updatedUserInRoom = adminResponse.getBody();
        assertThat(updatedUserInRoom).isNotNull();
        assertThat(updatedUserInRoom.isAdmin()).isTrue();
    }
}