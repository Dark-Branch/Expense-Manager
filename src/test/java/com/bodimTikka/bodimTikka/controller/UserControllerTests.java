package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.dto.UserDTO;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import com.bodimTikka.bodimTikka.service.AuthService;
import com.bodimTikka.bodimTikka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    private String baseUrl;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";
        userRepository.deleteAll();

        user = RoomControllerTests.saveUser(user, authService, "example@example.com");
        token = RoomControllerTests.setupSignedUserAndGetToken(user, restTemplate);
        System.out.println(user.getName() + user.getId() + user.getPassword());
        System.out.println(token);
    }

    @Test
    void testGetUserByName() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<UserDTO> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(baseUrl + "/" + user.getName(), HttpMethod.GET, entity, UserDTO.class);

        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(user.getId());
    }

    @Test
    void testGetUserByEmail() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<UserDTO> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(baseUrl + "/email?email=" + user.getEmail(), HttpMethod.GET, entity, UserDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testDeleteUser() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<User> entity = new HttpEntity<>(headers);

        ResponseEntity<?> response = restTemplate.exchange(baseUrl, HttpMethod.DELETE, entity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        UserDTO user1 = userService.getUserByEmail(user.getEmail());
        assertThat(user1).isNull();
    }
}
