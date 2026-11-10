package com.bank.customer.service;

import com.bank.customer.model.Customer;
import com.bank.customer.model.PersonalInfo;
import com.bank.customer.repository.CustomerRepository;
import com.bank.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer personalCustomer;

    @BeforeEach
    void setUp() {
        personalCustomer = new Customer();
        personalCustomer.setId("65f1a2b3c4d5e6f7a8b9c001");
        personalCustomer.setType("PERSONAL");
        personalCustomer.setStatus("ACTIVE");
        personalCustomer.setPersonalInfo(new PersonalInfo(
                "Juan", "Pérez", "DNI", "45678912", "juan.perez@email.com", "+51999888777"
        ));
    }

    @Test
    @DisplayName("Should emit all customers when gathering from database")
    void testGetAllCustomers() {
        // Arrange
        Mockito.when(customerRepository.findAll()).thenReturn(Flux.just(personalCustomer));

        // Act
        Flux<Customer> result = customerService.getAllCustomers();

        // Assert using StepVerifier
        StepVerifier.create(result)
                .expectNext(personalCustomer)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create customer and inject system timestamps")
    void testCreateCustomer() {
        // Arrange
        Mockito.when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(personalCustomer));

        // Act
        Mono<Customer> result = customerService.createCustomer(personalCustomer);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(customer -> 
                        customer.getId().equals("65f1a2b3c4d5e6f7a8b9c001") &&
                        customer.getCreatedAt() != null &&
                        customer.getUpdatedAt() != null
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update an existing customer fields fluently")
    void testUpdateCustomer() {
        // Arrange
        Customer updatedDetails = new Customer();
        updatedDetails.setStatus("INACTIVE");
        updatedDetails.setPersonalInfo(personalCustomer.getPersonalInfo());

        Mockito.when(customerRepository.findById("65f1a2b3c4d5e6f7a8b9c001")).thenReturn(Mono.just(personalCustomer));
        Mockito.when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(personalCustomer));

        // Act
        Mono<Customer> result = customerService.updateCustomer("65f1a2b3c4d5e6f7a8b9c001", updatedDetails);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(customer -> "INACTIVE".equals(customer.getStatus()))
                .verifyComplete();
    }
}
