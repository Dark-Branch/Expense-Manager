package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.RoomRepository;
import com.bodimTikka.bodimTikka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

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
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {
        PaymentRequestDTO request = buildPaymentRequest(false, BigDecimal.valueOf(100));

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

    private PaymentRequestDTO buildPaymentRequest(boolean isRepayment, BigDecimal amount) {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRoomId(room.getId());
        request.setPayerId(payer.getId());
        request.setRecipientIds(List.of(recipient1.getId(), recipient2.getId()));
        request.setTotalAmount(amount);
        request.setRepayment(isRepayment);
        return request;
    }
}
