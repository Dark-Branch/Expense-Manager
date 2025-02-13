package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void testSuccessfulRegistrationAndLoginFlow() {
        // signup
        SignupRequest signupRequest = new SignupRequest("John Doe", "john@test.com", "password123");
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(
                "/api/auth/signup",
                signupRequest,
                String.class
        );
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(signupResponse.getBody()).contains("User registered successfully");

        // login
        LoginRequest loginRequest = new LoginRequest("john@test.com", "password123");
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                String.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains("token");
    }

    @Test
    void testRegistrationWithDuplicateEmail() {
        restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("John", "duplicate@test.com", "password"),
                String.class);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("Jane", "duplicate@test.com", "password"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Email is already taken");
    }

    @Test
    void testLoginWithInvalidCredentials() {
        restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("John", "john@test.com", "correctpass"),
                String.class);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest("john@test.com", "wrongpass"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // FIXME: should this be forbidden or unauthorized
    @Test
    void testAccessProtectedResourceWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/payments", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // TODO: add more tests to do validation parts like 'pass' is not long enough to be a password
    @Test
    void testAccessProtectedResourceWithValidToken() {
        // Register and login
        restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("John", "john@test.com", "password"),
                String.class);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest("john@test.com", "password"),
                String.class);

        String token = extractToken(loginResponse.getBody());

        // protected resource
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                "/api/payments",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testAccessProtectedResourceWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/payments",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // TODO: this need to be unauthorized
    @Test
    void testRegistrationWithMissingFields() {
        // missing name
        ResponseEntity<String> response1 = restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest(null, "test@test.com", "pass"),
                String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // missing email
        ResponseEntity<String> response2 = restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("John", null, "pass"),
                String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // missing password
        ResponseEntity<String> response3 = restTemplate.postForEntity("/api/auth/signup",
                new SignupRequest("John", "test@test.com", null),
                String.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    static String extractToken(String responseBody) {
        return responseBody.split("\"token\":\"")[1].split("\"")[0];
    }

    private record SignupRequest(String name, String email, String password) {}
    private record LoginRequest(String email, String password) {}
}