package com.bodimtikka.controller;

import com.bodimtikka.model.Room;
import com.bodimtikka.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Room room;

    @BeforeEach
    public void setup() {
        roomRepository.deleteAll();

        room = new Room();
        room.setName("Test Room");
        roomRepository.save(room);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    public void testRoomWorkflow() throws Exception {
        // --- Create room ---
        Room newRoom = new Room();
        newRoom.setName("New Room");

        mockMvc.perform(post("/api/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRoom)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Room")))
                .andExpect(jsonPath("$.id").exists());

        // --- Get room by ID ---
        mockMvc.perform(get("/api/rooms/" + room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Room")));

        // --- List all rooms ---
        mockMvc.perform(get("/api/rooms/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Room")));

        // --- Delete room ---
        mockMvc.perform(delete("/api/rooms/delete/" + room.getId()))
                .andExpect(status().isOk());

        // --- Verify deletion ---
        mockMvc.perform(get("/api/rooms/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    public void testGetRoomByName() throws Exception {
        mockMvc.perform(get("/api/rooms/name/" + room.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Room")))
                .andExpect(jsonPath("$.id").exists());
    }
}
