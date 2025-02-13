package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.DTO.RoomPairBalanceDTO;
import com.bodimTikka.bodimTikka.DTO.RoomPaymentLogDTO;
import com.bodimTikka.bodimTikka.model.*;
import com.bodimTikka.bodimTikka.repository.*;
import com.bodimTikka.bodimTikka.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTests {

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
    @Autowired
    private PaymentRecordRepository paymentRecordRepository;
    @Autowired
    private PaymentService paymentService;


    private Room room;
    private User payer;
    private User recipient1;
    private User recipient2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roomRepository.deleteAll();
        paymentRepository.deleteAll();
        paymentRecordRepository.deleteAll();

        room = roomRepository.save(new Room("Test Room"));

        payer = userRepository.save(new User("Payer"));
        recipient1 = userRepository.save(new User("Recipient1"));
        recipient2 = userRepository.save(new User("Recipient2"));

        UserInRoom first = new UserInRoom(payer, room, "payer");
        UserInRoom second = new UserInRoom(recipient1, room, "eka");
        UserInRoom third = new UserInRoom(recipient2, room, "deka");

        userInRoomRepository.saveAll(List.of(first, second, third));
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {
        // make sure to cast to long else fked up
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(1.00));

        ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity("/payments/create", request, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPayment = response.getHeaders().getLocation();
        ResponseEntity<Payment> getResponse = restTemplate
                .getForEntity(locationOfNewPayment, Payment.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("payer Id does not belong to room");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldThrowErrorWhenRecipientDoesNotBelongToRoom() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setRecipientIds(List.of((long) -1));  // Invalid

        ResponseEntity<Map> response = restTemplate.postForEntity("/payments/create", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Recipient Id does not belong to room");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
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

    @Test
    public void shouldReturnPaymentsForRoom() {
        PaymentRequestDTO request1 = buildPaymentRequest(BigDecimal.valueOf(75));
        PaymentRequestDTO request2 = buildPaymentRequest(BigDecimal.valueOf(50));

        paymentService.createPayment(request1);
        paymentService.createPayment(request2);

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "?limit=20",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<RoomPaymentLogDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(2);
        // time stamp is ordered desc
        assertThat(response.getBody().get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(response.getBody().get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(75));
    }

    @Test
    public void shouldReturnEmptyListWhenNoPaymentsExistForRoom() {
        ResponseEntity<List<Payment>> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "?limit=5&page=0",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Payment>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(0);
    }

    @Test
    void testGetPaymentLogBetweenUsers_ValidUsersInRoom() {
        User user1 = userRepository.save(new User("Alice"));
        User user2 = userRepository.save(new User("Bob"));

        Room room = roomRepository.save(new Room("Test Room"));

        userInRoomRepository.save(new UserInRoom(user1, room, "one"));
        userInRoomRepository.save(new UserInRoom(user2, room, "two"));

        Payment payment1 = new Payment(room, BigDecimal.valueOf(100.00));
        payment1 = paymentRepository.save(payment1);
        Payment payment2 = new Payment(room, BigDecimal.valueOf(50.00));
        payment2 = paymentRepository.save(payment2);

        PaymentRecord record1 = PaymentRecord.builder().
                fromUser(user1).
                toUser(user2).
                amount(payment1.getAmount()).
                isCredit(false).
                payment(payment1).build();
        paymentRecordRepository.save(record1);
        PaymentRecord record2 = PaymentRecord.builder().
                fromUser(user2).
                toUser(user1).
                amount(payment2.getAmount()).
                isCredit(true).
                payment(payment2).build();
        paymentRecordRepository.save(record2);

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomPaymentLogDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void testGetPaymentLogBetweenUsers_NoPayments() {
        User user1 = userRepository.save(new User("Alice"));
        User user2 = userRepository.save(new User("Bob"));

        userInRoomRepository.save(new UserInRoom(user1, room, "one"));
        userInRoomRepository.save(new UserInRoom(user2, room, "two"));

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomPaymentLogDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }


    @Test
    void testGetPaymentLogBetweenUsers_UserNotInRoom() {
        User user1 = userRepository.save(new User("Alice"));
        User user2 = userRepository.save(new User("Bob"));

        userInRoomRepository.save(new UserInRoom(user1, room, "one"));

        ResponseEntity<String> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Users do not belong to the room");
    }

    @Test
    void testGetPaymentLogBetweenUsers_RoomNotFound() {
        // non-existing room ID
        ResponseEntity<String> response = restTemplate.exchange(
                "/payments/room/99999/users?user1=1&user2=2",
                HttpMethod.GET,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Room not found");
    }

    @Test
    void testGetPaymentLogBetweenUsers_MissingUserIds() {
        // Create a room
        Room room = roomRepository.save(new Room("Test Room"));

        // Call endpoint without user parameters
        ResponseEntity<String> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "/users",
                HttpMethod.GET,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Required parameter 'user1' is missing.");
    }

    @Test
    public void shouldReturnEmptyPairListWhenNoPaymentsExist() {
        ResponseEntity<RoomPairBalanceDTO[]> response = restTemplate.getForEntity(
                "/payments/room/" + room.getId() + "/balances",
                RoomPairBalanceDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    public void shouldReturnCorrectBalancesAfterSinglePayment() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(30.00));

        restTemplate.postForEntity("/payments/create", request, PaymentResponseDTO.class);

        ResponseEntity<List<RoomPairBalanceDTO>> response = restTemplate.exchange(
                "/payments/room/" + room.getId() + "/balances",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RoomPairBalanceDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnUpdatedBalancesAfterMultiplePayments() {
        restTemplate.postForEntity("/payments/create", buildPaymentRequest(BigDecimal.valueOf(30.00)), PaymentResponseDTO.class);
        restTemplate.postForEntity("/payments/create", buildRepaymentRequest(recipient1, BigDecimal.valueOf(10.00)), PaymentResponseDTO.class);

        ResponseEntity<RoomPairBalanceDTO[]> response = restTemplate.getForEntity(
                "/payments/room/" + room.getId() + "/balances",
                RoomPairBalanceDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);
    }

    private PaymentRequestDTO buildRepaymentRequest(User payer, BigDecimal amount) {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRoomId(room.getId());
        request.setPayerId(payer.getId());
        request.setRecipientIds(List.of(this.payer.getId()));
        request.setTotalAmount(amount);
        request.setRepayment(true);
        return request;
    }

    @Test
    public void shouldReturnNotFoundForNonExistentRoom() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/payments/room/999999/balances",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
