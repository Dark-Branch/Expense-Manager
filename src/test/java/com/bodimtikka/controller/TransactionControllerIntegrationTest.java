package com.bodimtikka.controller;

import com.bodimtikka.dto.TransactionCreateRequestDTO;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.UserRoom;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.TransactionRepository;
import com.bodimtikka.repository.UserRoomRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRoomRepository userRoomRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRoom sender;
    private UserRoom receiver;
    private Room room;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();
        userRoomRepository.deleteAll();
        roomRepository.deleteAll();

        sender = new UserRoom();
        sender.setNickname("Alice");
        userRoomRepository.save(sender);

        receiver = new UserRoom();
        receiver.setNickname("Bob");
        userRoomRepository.save(receiver);

        room = new Room();
        room.setName("Test Room");
        roomRepository.save(room);
    }

    @Test
    @WithMockUser(username = "alice@example.com", roles = {"USER"})
    public void testTransactionWorkflow() throws Exception {
        // --- Create transaction ---
        TransactionCreateRequestDTO request = new TransactionCreateRequestDTO(
                room.getId(),
                "Dinner",
                new BigDecimal("100.50"),
                List.of(sender.getId()),
                List.of(new BigDecimal("100.50")),
                List.of(receiver.getId()),
                List.of(new BigDecimal("100.50"))
        );

        mockMvc.perform(post("/api/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Dinner")))
                .andExpect(jsonPath("$.amount", is(100.50)))
                .andExpect(jsonPath("$.roomId", is(room.getId().intValue())))
                .andExpect(jsonPath("$.participants", hasSize(2)))
                .andExpect(jsonPath("$.participants[?(@.role == 'SENDER')].participantId", contains(sender.getId().intValue())))
                .andExpect(jsonPath("$.participants[?(@.role == 'RECEIVER')].participantId", contains(receiver.getId().intValue())));

        // --- List transactions by room ---
        mockMvc.perform(get("/api/transactions/room/" + room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Dinner")));

        // --- List transactions by participant ---
        mockMvc.perform(get("/api/transactions/participant/" + sender.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].participants[?(@.role == 'SENDER')].participantId", contains(sender.getId().intValue())));
    }
}
