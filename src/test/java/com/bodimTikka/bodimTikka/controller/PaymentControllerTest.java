package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.model.UserInRoom;
import com.bodimTikka.bodimTikka.repository.PaymentRepository;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserInRoomRepository;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserInRoomRepository userInRoomRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private Room room;
    private User payer;
    private User recipient1;
    private User recipient2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roomRepository.deleteAll();

        room = roomRepository.save(new Room("Test Room"));

        payer = userRepository.save(new User("Payer"));
        recipient1 = userRepository.save(new User("Recipient1"));
        recipient2 = userRepository.save(new User("Recipient2"));

        UserInRoom first = new UserInRoom(payer, room);
        UserInRoom second = new UserInRoom(recipient1, room);
        UserInRoom third = new UserInRoom(recipient2, room);

        userInRoomRepository.saveAll(List.of(first, second, third));
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));

        ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity("/payments/create", request, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRoomId()).isEqualTo(room.getId());
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    public void shouldGetAllPaymentsSuccessfully() {
        ResponseEntity<PaymentResponseDTO[]> response = restTemplate.exchange(
                "/payments",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                PaymentResponseDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
    }

    private PaymentRequestDTO buildPaymentRequest(BigDecimal amount) {
        return buildPaymentRequest(false, amount);
    }

    private PaymentRequestDTO buildPaymentRequest(boolean isRepayment, BigDecimal amount) {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRoomId(room.getId());
        request.setPayerId(payer.getId());
        request.setRecipientIds(List.of(recipient1.getId(), recipient2.getId()));
        request.setTotalAmount(amount);
        request.setRepayment(isRepayment);
        return request;
    }

    @Test
    public void shouldThrowErrorWhenRoomNotFound() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setRoomId((long) -1);  // Invalid

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments/create", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody().get("message")).isEqualTo("Room not found");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
    }


    @Test
    public void shouldThrowErrorWhenUsingNonRoomerPayer() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setPayerId((long) -1);  // Invalid

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments/create", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("payer Id does not belong to room");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldThrowErrorWhenRecipientDoesNotBelongToRoom() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setRecipientIds(List.of((long) -1));  // Invalid

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments/create", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Recipient Id does not belong to room");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldThrowErrorWhenTotalAmountIsZero() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.ZERO);

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments/create", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // this is handled in validation fw
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldThrowErrorWhenRequestIsInvalid() {
        PaymentRequestDTO request = new PaymentRequestDTO();

        ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity("/payments/create", request, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldReturnEmptyListWhenNoPaymentsExist() {
        // Clear out the payments before testing
        paymentRepository.deleteAll();

        ResponseEntity<PaymentResponseDTO[]> response = restTemplate.exchange(
                "/payments",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                PaymentResponseDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0); // No payments in the system
    }

}
