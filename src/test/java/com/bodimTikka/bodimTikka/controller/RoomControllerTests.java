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

        user = saveUser(user, authService);
        token = setupSignedUserAndGetToken(user, restTemplate);

        testRoom = new Room();
        testRoom.setName("Test Room");

        testRoom = roomRepository.save(testRoom);
    }

    public static String setupSignedUserAndGetToken(User user, TestRestTemplate restTemplate) {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                LoginRequest.builder()
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .build(),
                String.class
        );
        assertThat(loginResponse.getBody()).isNotNull();
        return AuthControllerTests.extractToken(loginResponse.getBody());
    }

    public static User saveUser(User user, AuthService authService){
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
        // this changes when saving
        user.setPassword(password);
        return user;
    }

    public static HttpHeaders getHttpHeadersWithToken(String token) {
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

        HttpHeaders headers = getHttpHeadersWithToken(token);
        HttpEntity<Room> entity = new HttpEntity<>(newRoom, headers);

        ResponseEntity<Room> response = restTemplate.exchange(BaseURL, HttpMethod.POST, entity, Room.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(newRoom.getName());
    }

    // TODO: test without auth
    @Test
    void testDeleteRoom() {
        addUserAndRoomRecordToUserInRoom(testRoom);
        HttpHeaders headers = getHttpHeadersWithToken(token);
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
        HttpHeaders headers = getHttpHeadersWithToken(token);
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
        HttpHeaders headers = getHttpHeadersWithToken(token);
        HttpEntity<Room> entity = new HttpEntity<>(testRoom, headers);

        ResponseEntity<List> response = restTemplate.exchange(BaseURL + "/" + testRoom.getId() + "/users", HttpMethod.GET, entity, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testGetRoomsByUserId_ValidRoomerId() {
        Room testRoom1 = new Room("Test Room 1");
        Room testRoom2 = new Room("Test Room 2");

        addUserToARoomUsingRepositories(testRoom1, false);
        addUserToARoomUsingRepositories(testRoom2, false);

        HttpHeaders headers = getHttpHeadersWithToken(token);
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

    private void addUserToARoomUsingRepositories(Room testRoom, boolean isAdmin) {
        testRoom = roomRepository.save(testRoom);

        UserInRoom userInRoom1 = new UserInRoom();
        userInRoom1.setRoom(testRoom);
        userInRoom1.setUser(user);
        userInRoom1.setName("yow");
        userInRoom1.setStillAMember(true);
        userInRoom1.setAdmin(isAdmin);
        userInRoom1 = userInRoomRepository.save(userInRoom1);
    }

    @Test
    void testGetRoomByRoomerId_InvalidRoomerId() {
        HttpHeaders headers = getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Room> response = restTemplate.exchange(BaseURL + "/roomer/99999", HttpMethod.GET, entity, Room.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // TODO: these all will change when we use principal object
    // TODO: when adding those, write tests for various cases of that too
    @Test
    void testAddUserToRoom_ValidRegisteredUser() {
        addUserAndRoomRecordToUserInRoom(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);

        AddUserRequestDTO request = new AddUserRequestDTO(user1.getId(), "John", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<UserInRoom> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                UserInRoom.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom userInRoom = response.getBody();
        assertThat(userInRoom).isNotNull();
        assertThat(userInRoom.getUser().getId()).isEqualTo(user1.getId());
        assertThat(userInRoom.getName()).isEqualTo("John");
        assertThat(userInRoom.isRegistered()).isTrue();
    }

    @Disabled
    @Test
    void testAddUserToRoom_ValidUnregisteredUser() {
        User user1 = new User("John");
        user1 = userRepository.save(user1);
        UserInRoom userInRoom1 = new UserInRoom(user1, testRoom, "pakaya");
        userInRoomRepository.save(userInRoom1);

        AddUserRequestDTO request = new AddUserRequestDTO(null, "Guest", false);
        ResponseEntity<UserInRoom> response = restTemplate.postForEntity(
                BaseURL + testRoom.getId() + "/users"+ "?senderId=" + user1.getId(), request, UserInRoom.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom userInRoom = response.getBody();
        assertThat(userInRoom).isNotNull();
        assertThat(userInRoom.getUser()).isNull();
        assertThat(userInRoom.getName()).isEqualTo("Guest");
        assertThat(userInRoom.isRegistered()).isFalse();
    }

    @Test
    void testAddUserToRoom_MissingName() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(user1.getId(), "", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Name cannot be empty");
    }

    private void addUserToARoomAsAdminUsingRepositories(Room testRoom) {
        addUserToARoomUsingRepositories(testRoom, true);
    }

    // TODO: i feel is registered can be managed internally, at least for registered ones
    @Test
    void testAddUserToRoom_MissingUserIdForRegisteredUser() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(null, "John", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User ID is required for registered users");
    }

    @Test
    void testAddUserToRoom_UserIdProvidedForUnregisteredUser() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(user.getId(), "Guest", false);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User ID must be null for unregistered users");
    }

    @Test
    void testAddUserToRoom_UserAlreadyInRoom() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(user1.getId(), "John", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<UserInRoom> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                UserInRoom.class
        );

        // Attempting to add the same user again should fail
        ResponseEntity<String> duplicateResponse = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicateResponse.getBody()).contains("User is already in the room");
    }

    @Test
    void testAddUserToRoom_InvalidUserId() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(999L, "John", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BaseURL + "/" + testRoom.getId() + "/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("User with ID 999 does not exist");
    }

    @Test
    void testAddUserToRoom_InvalidRoomId() {
        addUserToARoomAsAdminUsingRepositories(testRoom);

        User user1 = new User("John");
        user1 = userRepository.save(user1);

        HttpHeaders headers = getHttpHeadersWithToken(token);
        AddUserRequestDTO request = new AddUserRequestDTO(user1.getId(), "John", true);
        HttpEntity<AddUserRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BaseURL + "/999/users" ,
                HttpMethod.POST,
                entity,
                String.class
        );

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
                BaseURL + testRoom.getId() + "/users/" + user.getId() + "/assignAdmin", null, UserInRoom.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserInRoom updatedUserInRoom = adminResponse.getBody();
        assertThat(updatedUserInRoom).isNotNull();
        assertThat(updatedUserInRoom.isAdmin()).isTrue();
    }
}