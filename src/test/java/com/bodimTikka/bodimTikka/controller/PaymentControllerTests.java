package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.DTO.RoomPairBalanceDTO;
import com.bodimTikka.bodimTikka.DTO.RoomPaymentLogDTO;
import com.bodimTikka.bodimTikka.model.*;
import com.bodimTikka.bodimTikka.repository.*;
import com.bodimTikka.bodimTikka.service.AuthService;
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
    @Autowired
    private AuthService authService;


    private Room room;
    private User user;
    private User recipient1;
    private User recipient2;
    private String token;
    private String baseURL = "/api/payments";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roomRepository.deleteAll();
        paymentRepository.deleteAll();
        paymentRecordRepository.deleteAll();

        room = roomRepository.save(new Room("Test Room"));

        user = RoomControllerTests.saveUser(user, authService, "example@example.com");
        token = RoomControllerTests.setupSignedUserAndGetToken(user, restTemplate);

        recipient1 = userRepository.save(new User("Recipient1"));
        recipient2 = userRepository.save(new User("Recipient2"));

        UserInRoom first = new UserInRoom(user, room, "payer");
        UserInRoom second = new UserInRoom(recipient1, room, "eka");
        UserInRoom third = new UserInRoom(recipient2, room, "deka");

        userInRoomRepository.saveAll(List.of(first, second, third));
    }

    @Test
    public void shouldCreatePaymentSuccessfully() {
        // make sure to cast to long else fked up
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(1.00));

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(baseURL +"/create", HttpMethod.POST, entity, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPayment = response.getHeaders().getLocation();

        ResponseEntity<Payment> getResponse = restTemplate
                .exchange(locationOfNewPayment, HttpMethod.GET, entity, Payment.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
    }

    @Test
    public void shouldCreatePaymentWithDescriptionSuccessfully() {
        String expectedDescription = "Test payment description";
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(1.00));
        request.setDescription(expectedDescription); // Set the description

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                baseURL + "/create", HttpMethod.POST, entity, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPayment = response.getHeaders().getLocation();

        ResponseEntity<Payment> getResponse = restTemplate.exchange(
                locationOfNewPayment, HttpMethod.GET, entity, Payment.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
        assertThat(getResponse.getBody().getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    public void shouldReturnErrorMessageWhenRequestSenderNotInRoom() {
        // another user who is not in room
        User newUser = new User();
        newUser = RoomControllerTests.saveUser(newUser, authService, "newEmail@email.com");
        token = RoomControllerTests.setupSignedUserAndGetToken(newUser, restTemplate);

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(1.00));
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseURL + "/create",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertThat(response.getBody().get("message")).isEqualTo("Cannot add payments to room you are not a member of");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldGetAllPaymentsSuccessfully() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(headers);
        ResponseEntity<PaymentResponseDTO[]> response = restTemplate.exchange(
                baseURL,
                HttpMethod.GET,
                entity,
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
        request.setPayerId(user.getId());
        request.setRecipientIds(List.of(recipient1.getId(), recipient2.getId()));
        request.setTotalAmount(amount);
        request.setRepayment(isRepayment);
        return request;
    }

    @Test
    public void shouldThrowErrorWhenRoomNotFound() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setRoomId((long) -1);  // Invalid

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody().get("message")).isEqualTo("Room not found");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldThrowErrorWhenUsingNonRoomerPayer() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(100));
        request.setPayerId((long) -1);  // Invalid

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, Map.class);

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

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Recipient Id does not belong to room");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldThrowErrorWhenTotalAmountIsZero() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.ZERO);

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(baseURL + "/create",HttpMethod.POST, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // this is handled in validation fw
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldThrowErrorWhenRequestIsInvalid() {
        PaymentRequestDTO request = new PaymentRequestDTO();

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, PaymentResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldReturnEmptyListWhenNoPaymentsExist() {
        // Clear out the payments before testing
        paymentRepository.deleteAll();

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PaymentResponseDTO[]> response = restTemplate.exchange(
                baseURL,
                HttpMethod.GET,
                entity,
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

        paymentService.createPayment(request1, user.getEmail());
        paymentService.createPayment(request2, user.getEmail());

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "?limit=20",
                HttpMethod.GET,
                entity,
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
    public void shouldReturnErrorMessageWhenRequestSenderNotInRoomTriesToViewPayments() {
        // Create a new user who is NOT in the room
        User newUser = new User();
        newUser = RoomControllerTests.saveUser(newUser, authService, "newUser@email.com");
        token = RoomControllerTests.setupSignedUserAndGetToken(newUser, restTemplate);

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "?limit=20",
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertThat(response.getBody().get("message")).isEqualTo("Cannot view payments for a room you are not a member of");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void shouldReturnEmptyListWhenNoPaymentsExistForRoom() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Payment>> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "?limit=5&page=0",
                HttpMethod.GET,
                entity,
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

        userInRoomRepository.save(new UserInRoom(user1, room, "one"));
        userInRoomRepository.save(new UserInRoom(user2, room, "two"));

        createPayment(user2, user1, 50.00);
        createPayment(user1, user2, 100.00);

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RoomPaymentLogDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    private void createPayment(User fromUser, User toUser, double amount) {
        Payment payment1 = new Payment(room, BigDecimal.valueOf(amount));
        payment1 = paymentRepository.save(payment1);
        PaymentRecord record1 = PaymentRecord.builder().
                fromUser(fromUser).
                toUser(toUser).
                amount(payment1.getAmount()).
                isCredit(false).
                payment(payment1).build();
        paymentRecordRepository.save(record1);
    }

    @Test
    void testGetPaymentLogBetweenUsers_NoPayments() {
        User user1 = userRepository.save(new User("Alice"));
        User user2 = userRepository.save(new User("Bob"));

        userInRoomRepository.save(new UserInRoom(user1, room, "one"));
        userInRoomRepository.save(new UserInRoom(user2, room, "two"));

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<RoomPaymentLogDTO>> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                entity,
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

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/users?user1=" + user1.getId() + "&user2=" + user2.getId(),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Users do not belong to the room");
    }

    @Test
    void testGetPaymentLogBetweenUsers_RoomNotFound() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // non-existing room ID
        ResponseEntity<String> response = restTemplate.exchange(
                baseURL + "/room/99999/users?user1=1&user2=2",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Room not found");
    }

    @Test
    void testGetPaymentLogBetweenUsers_MissingUserIds() {
        // Create a room
        Room room = roomRepository.save(new Room("Test Room"));

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Call endpoint without user parameters
        ResponseEntity<String> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/users",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Required parameter 'user1' is missing.");
    }

    @Test
    public void shouldReturnEmptyPairListWhenNoPaymentsExist() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RoomPairBalanceDTO[]> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/balances",
                HttpMethod.GET,
                entity,
                RoomPairBalanceDTO[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    public void shouldReturnCorrectBalancesAfterSinglePayment() {
        PaymentRequestDTO request = buildPaymentRequest(BigDecimal.valueOf(30.00));

        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(baseURL + "/create",
                HttpMethod.POST, entity, PaymentResponseDTO.class);

        ResponseEntity<List<RoomPairBalanceDTO>> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/balances",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RoomPairBalanceDTO>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnUpdatedBalancesAfterMultiplePayments() {
        PaymentRequestDTO request1 = buildPaymentRequest(BigDecimal.valueOf(30.00));
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request1, headers);

        restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, PaymentResponseDTO.class);

        PaymentRequestDTO request2 = buildPaymentRequest(BigDecimal.valueOf(30.00));
        entity = new HttpEntity<>(request2, headers);
        restTemplate.exchange(baseURL + "/create", HttpMethod.POST, entity, PaymentResponseDTO.class);

        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<RoomPairBalanceDTO[]> response = restTemplate.exchange(
                baseURL + "/room/" + room.getId() + "/balances",
                HttpMethod.GET,
                getEntity,
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
        request.setRecipientIds(List.of(this.user.getId()));
        request.setTotalAmount(amount);
        request.setRepayment(true);
        return request;
    }

    @Test
    public void shouldReturnNotFoundForNonExistentRoom() {
        HttpHeaders headers = RoomControllerTests.getHttpHeadersWithToken(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseURL + "/room/999999/balances",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
