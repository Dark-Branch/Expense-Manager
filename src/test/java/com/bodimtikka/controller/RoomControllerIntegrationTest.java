package com.bodimtikka.controller;

import com.bodimtikka.dto.room.RoomRequest;
import com.bodimtikka.dto.room.RoomResponse;
import com.bodimtikka.dto.room.RoomSummaryResponse;
import com.bodimtikka.model.User;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserAuthRepository;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.repository.ParticipantRepository;
import com.bodimtikka.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
@Transactional
public class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    public void setup() {
        participantRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
        userAuthRepository.deleteAll();

        // --- Create test user ---
        testUser = new User();
        testUser.setName("Kevin Sanjula");
        testUser.setEmail("kevin@example.com");
        userRepository.save(testUser);

        // --- Generate JWT ---
        jwtToken = jwtService.generateToken(testUser.getId(), Collections.singletonList("ROLE_USER"));
    }

    // -------------------- POST /api/rooms --------------------
    @Test
    public void testCreateRoomSuccess() throws Exception {
        RoomRequest request = new RoomRequest("My Room");

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("My Room")))
                .andExpect(jsonPath("$.owner.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andExpect(jsonPath("$.participants[0].userId", is(testUser.getId().intValue())));
    }

    @Test
    public void testCreateRoomWithoutName() throws Exception {
        RoomRequest request = new RoomRequest(""); // empty name

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateRoomWithoutJwt() throws Exception {
        RoomRequest request = new RoomRequest("Unauthorized Room");

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateRoomWithInvalidJwt() throws Exception {
        RoomRequest request = new RoomRequest("Invalid JWT Room");

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer INVALID_TOKEN")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateMultipleRoomsForSameUser() throws Exception {
        RoomRequest first = new RoomRequest("Room One");
        RoomRequest second = new RoomRequest("Room Two");

        // Create first room
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        // Create second room
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Room Two")))
                .andExpect(jsonPath("$.owner.id", is(testUser.getId().intValue())));
    }

    // -------------------- GET /api/rooms/{id} --------------------
    @Test
    public void testGetRoomByIdSuccess() throws Exception {
        // --- Create a room for the test user ---
        RoomRequest request = new RoomRequest("Test Room");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RoomSummaryResponse roomResponse = objectMapper.readValue(response, RoomSummaryResponse.class);

        // --- Fetch the room by ID ---
        mockMvc.perform(get("/api/rooms/" + roomResponse.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(roomResponse.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Room")))
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andExpect(jsonPath("$.participants[0].nickname", is(testUser.getName())));
    }

    @Test
    public void testGetRoomByIdForbiddenForNonMember() throws Exception {
        // --- Create a room for the test user ---
        RoomRequest request = new RoomRequest("Private Room");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RoomResponse roomResponse = objectMapper.readValue(response, RoomResponse.class);

        // --- Create another user who is NOT a participant ---
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        userRepository.save(anotherUser);

        String otherJwt = jwtService.generateToken(anotherUser.getId(), Collections.singletonList("ROLE_USER"));

        // --- Attempt to fetch the room by non-member ---
        mockMvc.perform(get("/api/rooms/" + roomResponse.getId())
                        .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetRoomByIdNotFound() throws Exception {
        // Use a non-existent room ID
        mockMvc.perform(get("/api/rooms/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetRoomByIdWithoutJwt() throws Exception {
        // --- Create a room for the test user ---
        RoomRequest request = new RoomRequest("Room No JWT");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RoomResponse roomResponse = objectMapper.readValue(response, RoomResponse.class);

        // --- Attempt to fetch without JWT ---
        mockMvc.perform(get("/api/rooms/" + roomResponse.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetRoomByIdWithInvalidJwt() throws Exception {
        // --- Create a room for the test user ---
        RoomRequest request = new RoomRequest("Room Invalid JWT");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RoomResponse roomResponse = objectMapper.readValue(response, RoomResponse.class);

        // --- Attempt to fetch with invalid JWT ---
        mockMvc.perform(get("/api/rooms/" + roomResponse.getId())
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- GET /api/rooms --------------------
    @Test
    public void testGetUserRoomsSuccessSingleRoom() throws Exception {
        // Create a room for the test user
        RoomRequest request = new RoomRequest("My Room");
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("My Room")))
                .andExpect(jsonPath("$[0].participants", hasSize(1)))
                .andExpect(jsonPath("$[0].participants[0].userId").exists());
    }

    @Test
    public void testGetUserRoomsSuccessMultipleRooms() throws Exception {
        // Create multiple rooms
        RoomRequest room1 = new RoomRequest("Room One");
        RoomRequest room2 = new RoomRequest("Room Two");

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(room1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(room2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].participants[0].userId").exists())
                .andExpect(jsonPath("$[1].participants[0].userId").exists());
    }

    @Test
    public void testGetUserRoomsEmptyList() throws Exception {
        // No rooms for this user yet
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetUserRoomsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserRoomsWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserRoomsDifferentUserNoAccessToOtherRooms() throws Exception {
        // Create a room for testUser
        RoomRequest request = new RoomRequest("Private Room");
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Create a different user
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        userRepository.save(otherUser);
        String otherJwt = jwtService.generateToken(otherUser.getId(), Collections.singletonList("ROLE_USER"));

        // Other user should see empty list
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------- DELETE /api/rooms/{id} --------------------
    @Test
    public void testDeleteRoomSuccess() throws Exception {
        // Create room as test user
        RoomRequest createRequest = new RoomRequest("Room To Delete");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long roomId = objectMapper.readTree(response).path("id").asLong();

        // Delete room as owner
        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteRoomNotOwner() throws Exception {
        // Create room as test user
        RoomRequest createRequest = new RoomRequest("Owner Room");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long roomId = objectMapper.readTree(response).path("id").asLong();

        // Create another user
        User anotherUser = new User();
        anotherUser.setName("Other User");
        anotherUser.setEmail("other@example.com");
        userRepository.save(anotherUser);
        String otherJwt = jwtService.generateToken(anotherUser.getId(), Collections.singletonList("ROLE_USER"));

        // Attempt deletion as non-owner
        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteRoomNotFound() throws Exception {
        mockMvc.perform(delete("/api/rooms/9999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteRoomWithoutJwt() throws Exception {
        RoomRequest createRequest = new RoomRequest("Room Without JWT");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long roomId = objectMapper.readTree(response).path("id").asLong();

        // Attempt deletion without JWT
        mockMvc.perform(delete("/api/rooms/" + roomId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteRoomWithInvalidJwt() throws Exception {
        RoomRequest createRequest = new RoomRequest("Room Invalid JWT");
        String response = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long roomId = objectMapper.readTree(response).path("id").asLong();

        // Attempt deletion with invalid JWT
        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }
}
