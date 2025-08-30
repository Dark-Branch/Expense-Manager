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
    public void testRegisterAndLogin_withJwt() throws Exception {
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
                .andExpect(jsonPath("$.email", is("kevin@example.com")))
                .andExpect(jsonPath("$.token").isNotEmpty());

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
                .andExpect(jsonPath("$.email", is("kevin@example.com")))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // --- Missing Fields ---
    @Test
    public void testRegister_missingName() throws Exception {
        String json = """
            {
                "email": "kevin@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Name is required")));
    }

    @Test
    public void testRegister_missingEmail() throws Exception {
        String json = """
            {
                "name": "Kevin Sanjula",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Email is required")));
    }

    @Test
    public void testRegister_missingPassword() throws Exception {
        String json = """
            {
                "name": "Kevin Sanjula",
                "email": "kevin@example.com"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Password is required")));
    }

    // --- Invalid Fields ---
    @Test
    public void testRegister_nameTooShort() throws Exception {
        String json = """
            {
                "name": "Al",
                "email": "kevin@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Name must be between 3 and 50 characters")));
    }

    @Test
    public void testRegister_nameTooLong() throws Exception {
        String longName = "A".repeat(51);
        String json = String.format("""
            {
                "name": "%s",
                "email": "kevin@example.com",
                "password": "password123"
            }
            """, longName);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Name must be between 3 and 50 characters")));
    }

    @Test
    public void testRegister_invalidEmail() throws Exception {
        String json = """
            {
                "name": "Kevin Sanjula",
                "email": "kevin-at-example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Invalid email format")));
    }

    @Test
    public void testRegister_passwordTooShort() throws Exception {
        String json = """
            {
                "name": "Kevin Sanjula",
                "email": "kevin@example.com",
                "password": "123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", is("Password must be at least 6 characters")));
    }

    // --- Duplicate Email ---
    @Test
    public void testRegister_duplicateEmail() throws Exception {
        // Pre-save a user
        User user = new User();
        user.setName("Kevin Sanjula");
        user.setEmail("kevin@example.com");
        userRepository.save(user);

        String json = """
            {
                "name": "Another Kevin",
                "email": "kevin@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email already exists")));
    }
}
