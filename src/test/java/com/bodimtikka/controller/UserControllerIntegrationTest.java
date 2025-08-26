package com.bodimtikka.controller;

import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
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

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "kevin@example.com", roles = {"USER"})
    public void testUserCrudAndSearch() throws Exception {
        // --- Create a user directly in DB for testing ---
        User user = new User();
        user.setName("Kevin Sanjula");
        user.setEmail("kevin@example.com");
        userRepository.save(user);

        // --- Get user by ID ---
        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kevin Sanjula")))
                .andExpect(jsonPath("$.email", is("kevin@example.com")));

        // --- Search user by name ---
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "Kevin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Kevin Sanjula")));

        // --- Update user ---
        String updateJson = """
                {
                    "name": "Kevin S",
                    "email": "kevin.s@example.com"
                }
                """;

        mockMvc.perform(put("/api/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kevin S")))
                .andExpect(jsonPath("$.email", is("kevin.s@example.com")));

        // --- Delete user ---
        mockMvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().is4xxClientError());
    }
}
