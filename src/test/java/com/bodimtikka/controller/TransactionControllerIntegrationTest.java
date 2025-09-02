package com.bodimtikka.controller;

import com.bodimtikka.dto.transaction.TransactionCreateRequest;
import com.bodimtikka.dto.transaction.TransactionCreateRequest.SenderDTO;
import com.bodimtikka.model.Participant;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.User;
import com.bodimtikka.repository.ParticipantRepository;
import com.bodimtikka.repository.RoomRepository;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;
    private User testUser;
    private Room testRoom;
    private Participant senderParticipant;
    private Participant receiverParticipant;

    @BeforeEach
    public void setup() {
        participantRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        // --- Create test user ---
        testUser = new User();
        testUser.setName("Kevin Sanjula");
        testUser.setEmail("kevin@example.com");
        userRepository.save(testUser);

        // --- Create room ---
        testRoom = new Room();
        testRoom.setName("Test Room");
        testRoom.setOwner(testUser);
        roomRepository.save(testRoom);

        // --- Create participants ---
        senderParticipant = new Participant();
        senderParticipant.setUser(testUser);
        senderParticipant.setRoom(testRoom);
        participantRepository.save(senderParticipant);
        // Update inverse side
        testRoom.getParticipants().add(senderParticipant);

        receiverParticipant = new Participant();
        receiverParticipant.setUser(testUser); // can be a different user
        receiverParticipant.setRoom(testRoom);
        participantRepository.save(receiverParticipant);
        // Update inverse side
        testRoom.getParticipants().add(receiverParticipant);

        // --- Generate JWT ---
        jwtToken = jwtService.generateToken(testUser.getId(), List.of("ROLE_USER"));
    }

    // -------------------- POST /api/rooms/{roomId/transactions} --------------------
    // -------------------- SUCCESS --------------------
    @Test
    public void testCreateTransactionSuccess() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Dinner")))
                .andExpect(jsonPath("$.amount", is(3000)))
                .andExpect(jsonPath("$.participants", hasSize(2)));
    }

    // -------------------- VALIDATION --------------------
    @Test
    public void testCreateTransactionMissingDescription() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransactionNegativeAmount() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("-100"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("-100"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransactionEmptySenders() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransactionEmptyReceivers() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of()
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateTransactionSenderNotInRoom() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(999L, new BigDecimal("3000"))), // invalid participant
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Some senders are not part of this room")));
    }

    @Test
    public void testCreateTransactionReceiverNotInRoom() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(999L) // invalid receiver
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Some receivers are not part of this room")));
    }

    @Test
    public void testCreateTransactionSenderTotalMismatch() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("1000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Total sender amounts must equal transaction amount")));
    }

    // -------------------- AUTH --------------------
    @Test
    public void testCreateTransactionWithoutJwt() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateTransactionWithInvalidJwt() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer INVALID_TOKEN")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- GET /api/rooms/{roomId}/transactions --------------------
    @Test
    public void testGetTransactionsSuccess() throws Exception {
        // First, create a transaction
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Lunch",
                new BigDecimal("2000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("2000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Now GET transactions
        mockMvc.perform(get("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Lunch")))
                .andExpect(jsonPath("$[0].amount", is(2000)))
                .andExpect(jsonPath("$[0].participants", hasSize(2)));
    }

    @Test
    public void testGetTransactionsEmpty() throws Exception {
        // Room exists but no transactions
        mockMvc.perform(get("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetTransactionsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTransactionsWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTransactionsUserNotInRoom() throws Exception {
        // Create a new user not part of the room
        User otherUser = new User();
        otherUser.setName("Alice");
        otherUser.setEmail("alice@example.com");
        userRepository.save(otherUser);
        String otherJwt = jwtService.generateToken(otherUser.getId(), List.of("ROLE_USER"));

        mockMvc.perform(get("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isForbidden());
    }

    // -------------------- GET /api/rooms/{roomId}/transactions/participant/{participantId} --------------------

    // --- SUCCESS: participant has transactions ---
    @Test
    public void testGetTransactionsForParticipantSuccess() throws Exception {
        // Create a transaction first
        TransactionCreateRequest request = new TransactionCreateRequest(
                testRoom.getId(),
                "Dinner",
                new BigDecimal("3000"),
                List.of(new SenderDTO(senderParticipant.getId(), new BigDecimal("3000"))),
                List.of(receiverParticipant.getId())
        );

        mockMvc.perform(post("/api/rooms/{roomId}/transactions", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // GET transactions for sender participant
        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Dinner")))
                .andExpect(jsonPath("$[0].amount", is(3000)))
                .andExpect(jsonPath("$[0].participants", hasSize(2)));
    }

    // --- SUCCESS: participant has no transactions ---
    @Test
    public void testGetTransactionsForParticipantEmpty() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- UNAUTHORIZED: missing JWT ---
    @Test
    public void testGetTransactionsForParticipantNoJwt() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- UNAUTHORIZED: invalid JWT ---
    @Test
    public void testGetTransactionsForParticipantInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    // --- FORBIDDEN: user tries to fetch another participant's transactions ---
    @Test
    public void testGetTransactionsForParticipantForbidden() throws Exception {
        // Create another user not associated with senderParticipant
        User otherUser = new User();
        otherUser.setName("Alice");
        otherUser.setEmail("alice@example.com");
        userRepository.save(otherUser);

        String otherJwt = jwtService.generateToken(otherUser.getId(), List.of("ROLE_USER"));

        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + otherJwt))
                .andExpect(status().isForbidden());
    }

    // --- INVALID PARTICIPANT ID ---
    @Test
    public void testGetTransactionsForInvalidParticipant() throws Exception {
        Long invalidParticipantId = 999L; // does not exist

        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        testRoom.getId(), invalidParticipantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    // --- INVALID ROOM ID ---
    @Test
    public void testGetTransactionsForParticipantInvalidRoom() throws Exception {
        Long invalidRoomId = 999L; // does not exist

        mockMvc.perform(get("/api/rooms/{roomId}/transactions/participant/{participantId}",
                        invalidRoomId, senderParticipant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden()); // user is not member
    }
}
