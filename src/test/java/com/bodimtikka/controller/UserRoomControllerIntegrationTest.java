package com.bodimtikka.controller;

import com.bodimtikka.model.Participant;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.UserRoom;
import com.bodimtikka.repository.ParticipantRepository;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserRoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRoomRepository userRoomRepository;

    private Participant participant;
    private Room room;

    @BeforeEach
    void setup() {
        userRoomRepository.deleteAll();
        participantRepository.deleteAll();
        roomRepository.deleteAll();

        participant = new Participant();
        participant.setDisplayName("Kevin");
        participant = participantRepository.save(participant); // assign saved entity

        room = new Room();
        room.setName("Room 1");
        room = roomRepository.save(room); // assign saved entity
    }

    @Test
    @Transactional
    @WithMockUser(username = "kevin@example.com", roles = {"USER"})
    void testUserRoomWorkflow() throws Exception {

        // --- Add participant to room ---
        mockMvc.perform(post("/api/user-rooms/add")
                        .param("participantId", participant.getId().toString())
                        .param("roomId", room.getId().toString())
                        .param("nickname", "Kev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participant.id", is(participant.getId().intValue())))
                .andExpect(jsonPath("$.room.id", is(room.getId().intValue())))
                .andExpect(jsonPath("$.nickname", is("Kev")))
                .andExpect(jsonPath("$.stillAMember", is(true)));

        UserRoom userRoom = userRoomRepository.findAll().get(0);

        // --- List active participants in room ---
        mockMvc.perform(get("/api/user-rooms/room/{roomId}", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].participant.id", is(participant.getId().intValue())))
                .andExpect(jsonPath("$[0].nickname", is("Kev")))
                .andExpect(jsonPath("$[0].stillAMember", is(true)));

        // --- Remove participant from room (soft delete) ---
        mockMvc.perform(delete("/api/user-rooms/{id}", userRoom.getId()))
                .andExpect(status().isNoContent());

        // --- Verify participant is no longer active in the room ---
        mockMvc.perform(get("/api/user-rooms/room/{roomId}", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stillAMember", is(false)));

        // --- List rooms for participant (still returns inactive userRoom) ---
        mockMvc.perform(get("/api/user-rooms/participant/{participantId}", participant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stillAMember", is(false)));
    }
}
