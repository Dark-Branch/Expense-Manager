package com.bodimtikka.controller;

import com.bodimtikka.dto.participant.AddParticipantRequest;
import com.bodimtikka.model.Room;
import com.bodimtikka.model.User;
import com.bodimtikka.model.Participant;
import com.bodimtikka.repository.RoomRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ParticipantControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private JwtService jwtService;

    private User ownerUser;
    private User anotherUser;
    private String ownerToken;
    private Room testRoom;

    @BeforeEach
    public void setup() {
        participantRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        // Owner
        ownerUser = new User();
        ownerUser.setName("Owner User");
        ownerUser.setEmail("owner@example.com");
        userRepository.save(ownerUser);
        ownerToken = jwtService.generateToken(ownerUser.getId(), Collections.singletonList("ROLE_USER"));

        // Another registered user
        anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        userRepository.save(anotherUser);

        // Room owned by ownerUser
        testRoom = new Room();
        testRoom.setName("Test Room");
        testRoom.setOwner(ownerUser);
        roomRepository.save(testRoom);

        // Add owner as member
        participantRepository.save(new Participant(ownerUser, testRoom, "Owner Nick"));
    }

    // -------------------- POST /api/user-rooms/{roomId}/members/ --------------------

    @Test
    public void testAddRegisteredMemberSuccess() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("New Member");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is("New Member")))
                .andExpect(jsonPath("$.userId", is(anotherUser.getId().intValue())));
    }

    @Test
    public void testAddUnregisteredMemberSuccess() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(null); // unregistered
        request.setNickname("Temp Member");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is("Temp Member")))
                .andExpect(jsonPath("$.userId").doesNotExist());
    }

    @Test
    public void testAddMemberUnauthorized_NoJwt() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("Unauthorized");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAddMemberUnauthorized_InvalidJwt() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("Invalid JWT");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer INVALID_TOKEN")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAddMemberForbidden_RequesterNotInRoom() throws Exception {
        // new user not in the room
        User outsider = new User();
        outsider.setName("Outsider");
        outsider.setEmail("outsider@example.com");
        userRepository.save(outsider);
        String outsiderToken = jwtService.generateToken(outsider.getId(), Collections.singletonList("ROLE_USER"));

        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("Should Fail");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + outsiderToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAddMemberInvalidRoom() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("Wrong Room");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", 9999) // non-existing room
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddMemberInvalidUserId() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(9999L); // non-existing user
        request.setNickname("Invalid User");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddMemberMissingNickname() throws Exception {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname(""); // invalid nickname

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddDuplicateMember() throws Exception {
        // Add anotherUser first
        participantRepository.save(new Participant(anotherUser, testRoom, "Existing"));

        AddParticipantRequest request = new AddParticipantRequest();
        request.setUserId(anotherUser.getId());
        request.setNickname("Duplicate");

        mockMvc.perform(post("/api/user-rooms/{roomId}/members", testRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // -------------------- DELETE /api/user-rooms/{roomId}/members/{memberId} --------------------

    @Test
    public void testRemoveMemberSuccess() throws Exception {
        // Add another member first
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), member.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Participant updated = participantRepository.findById(member.getId()).orElseThrow();
        assertFalse(updated.isStillAMember());
    }

    @Test
    public void testRemoveMemberForbidden_NotOwner() throws Exception {
        // Add another member first
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));

        // JWT of member (not owner)
        String memberToken = jwtService.generateToken(anotherUser.getId(), Collections.singletonList("ROLE_USER"));

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), member.getId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testRemoveMemberInvalidRoom() throws Exception {
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", 9999, member.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRemoveMemberInvalidMemberId() throws Exception {
        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), 9999)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRemoveMemberAlreadyRemoved() throws Exception {
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));
        member.setStillAMember(false);
        participantRepository.save(member);

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), member.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRemoveMemberUnauthorized_NoJwt() throws Exception {
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), member.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRemoveMemberUnauthorized_InvalidJwt() throws Exception {
        Participant member = participantRepository.save(new Participant(anotherUser, testRoom, "Another Nick"));

        mockMvc.perform(delete("/api/user-rooms/{roomId}/members/{memberId}", testRoom.getId(), member.getId())
                        .header("Authorization", "Bearer INVALID_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

}
