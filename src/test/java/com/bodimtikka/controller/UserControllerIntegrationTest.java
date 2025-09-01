package com.bodimtikka.controller;

import com.bodimtikka.dto.user.UpdateUserRequest;
import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User normalUser;
    private User adminUser;
    private String normalJwt;
    private String adminJwt;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        // --- Normal user ---
        normalUser = new User();
        normalUser.setName("Normal User");
        normalUser.setEmail("user@example.com");
        userRepository.save(normalUser);
        normalJwt = jwtService.generateToken(normalUser.getId(), Collections.singletonList("ROLE_USER"));

        // --- Admin user ---
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        userRepository.save(adminUser);
        adminJwt = jwtService.generateToken(adminUser.getId(), Collections.singletonList("ROLE_ADMIN"));
    }

    // -------------------- GET /api/users/{id} --------------------
    @Test
    public void testGetUserSelf() throws Exception {
        mockMvc.perform(get("/api/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Normal User")))
                .andExpect(jsonPath("$.email", is("user@example.com")));
    }

    @Test
    public void testGetUserByAdmin() throws Exception {
        mockMvc.perform(get("/api/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Normal User")));
    }

    @Test
    public void testGetUserForbiddenForOtherUser() throws Exception {
        mockMvc.perform(get("/api/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/9999")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserNoJwt() throws Exception {
        mockMvc.perform(get("/api/users/" + normalUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- GET /api/users/search --------------------
    @Test
    public void testSearchUsersByAdmin() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "User")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testSearchUsersForbiddenForNormalUser() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "User")
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSearchUsersNoJwt() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "User"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSearchUsersEmptyResult() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "NonExistent")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------- PUT /api/users/{id} --------------------
    @Test
    public void testUpdateUserSelf() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated User", "updated@example.com");

        mockMvc.perform(put("/api/users/" + normalUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated User")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    public void testUpdateUserByAdmin() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Admin", "admin.updated@example.com");

        mockMvc.perform(put("/api/users/" + normalUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Admin")));
    }

    @Test
    public void testUpdateUserForbiddenForOtherUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Hacker", "hack@example.com");

        mockMvc.perform(put("/api/users/" + adminUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateUserNoJwt() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("No JWT", "nojwt@example.com");

        mockMvc.perform(put("/api/users/" + normalUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- DELETE /api/users/{id} --------------------
    @Test
    public void testDeleteUserByAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUserForbiddenForNormalUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + normalJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteUserNoJwt() throws Exception {
        mockMvc.perform(delete("/api/users/" + normalUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/9999")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isNotFound());
    }
}
