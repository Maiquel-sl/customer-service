package com.bank.customer.controller;

import com.bank.customer.model.Customer;
import com.bank.customer.model.PersonalInfo;
import com.bank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerService customerService;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId("12345");
        mockCustomer.setType("PERSONAL");
        mockCustomer.setStatus("ACTIVE");
        mockCustomer.setPersonalInfo(new PersonalInfo(
                "Maria", "López", "DNI", "98765432", "maria@email.com", "+511234567"
        ));
    }

    @Test
    @DisplayName("GET /api/customers - Should return a collection stream with HTTP 200 OK")
    void testGetAllCustomersEndpoint() {
        Mockito.when(customerService.getAllCustomers()).thenReturn(Flux.just(mockCustomer));

        webTestClient.get()
                .uri("/api/customers")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Customer.class)
                .hasSize(1)
                .contains(mockCustomer);
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Should return customer document when found")
    void testGetCustomerByIdFound() {
        Mockito.when(customerService.getCustomerById("12345")).thenReturn(Mono.just(mockCustomer));

        webTestClient.get()
                .uri("/api/customers/12345")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("12345")
                .jsonPath("$.type").isEqualTo("PERSONAL");
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Should return HTTP 404 NOT FOUND on empty publisher")
    void testGetCustomerByIdNotFound() {
        Mockito.when(customerService.getCustomerById("unknown")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/customers/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/customers - Should process data object and return HTTP 201 CREATED")
    void testCreateCustomerEndpoint() {
        Mockito.when(customerService.createCustomer(any(Customer.class))).thenReturn(Mono.just(mockCustomer));

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mockCustomer)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("12345");
    }
}
