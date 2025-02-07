package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/users";
        userRepository.deleteAll();
    }

    // TODO: handle duplicate errors
    @Test
    void testCreateUser() {
        User user = new User(null, "John Doe", "johndoe@example.com", "password123");

        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, user, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("johndoe@example.com");
    }

    @Test
    void testGetUserById() {
        User user = new User(null, "Alice", "alice@example.com", "secret");
        ResponseEntity<User> createdUserResponse = restTemplate.postForEntity(baseUrl, user, User.class);

        Long userId = createdUserResponse.getBody().getId();
        ResponseEntity<User> response = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setName("user");
        userRepository.save(user);
        ResponseEntity<User[]> response = restTemplate.getForEntity(baseUrl, User[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testGetUserByEmail() {
        User user = new User(null, "Bob", "bob@example.com", "pass123");
        restTemplate.postForEntity(baseUrl, user, User.class);

        ResponseEntity<User> response = restTemplate.getForEntity(baseUrl + "/email?email=bob@example.com", User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testDeleteUser() {
        User user = new User(null, "Charlie", "charlie@example.com", "mypass");
        ResponseEntity<User> createdUserResponse = restTemplate.postForEntity(baseUrl, user, User.class);

        Long userId = createdUserResponse.getBody().getId();
        restTemplate.delete(baseUrl + "/" + userId);

        ResponseEntity<User> response = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
