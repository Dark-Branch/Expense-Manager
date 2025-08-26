package com.bodimtikka.controller;

import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserAuthRepository;
import com.bodimtikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        userAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        // --- Register ---
        String registerJson = """
                {
                    "name": "Kevin Sanjula",
                    "email": "kevin@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kevin Sanjula")))
                .andExpect(jsonPath("$.email", is("kevin@example.com")));

        // Verify user saved in DB
        User user = userRepository.findByEmail("kevin@example.com").orElseThrow();
        assert passwordEncoder.matches("password123", user.getAuth().getPasswordHash());

        // --- Login ---
        String loginJson = """
                {
                    "email": "kevin@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Kevin Sanjula")))
                .andExpect(jsonPath("$.email", is("kevin@example.com")));
    }
}
