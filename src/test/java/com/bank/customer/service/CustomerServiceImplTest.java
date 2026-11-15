package com.bank.customer.service;

import com.bank.customer.messaging.CustomerEventPublisher;
import com.bank.customer.model.Customer;
import com.bank.customer.model.EnterpriseInfo;
import com.bank.customer.model.PersonalInfo;
import com.bank.customer.repository.CustomerHolderRepository;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerHolderRepository holderRepository;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer vipPersonalCustomer;
    private Customer pymeEnterpriseCustomer;

    @BeforeEach
    void setUp() {
        // Cliente Personal con perfil VIP
        vipPersonalCustomer = new Customer();
        vipPersonalCustomer.setId("65f1a2b3c4d5e6f7a8b9c001");
        vipPersonalCustomer.setType("PERSONAL");
        vipPersonalCustomer.setProfile("VIP");
        vipPersonalCustomer.setStatus("ACTIVE");
        vipPersonalCustomer.setPersonalInfo(new PersonalInfo(
                "Ana", "Gómez", "DNI", "87654321", "ana.vip@bank.com", "999111222"));

        // Cliente Empresarial con perfil PYME
        pymeEnterpriseCustomer = new Customer();
        pymeEnterpriseCustomer.setId("65f1a2b3c4d5e6f7a8b9c002");
        pymeEnterpriseCustomer.setType("ENTERPRISE");
        pymeEnterpriseCustomer.setProfile("PYME");
        pymeEnterpriseCustomer.setStatus("ACTIVE");
        pymeEnterpriseCustomer.setEnterpriseInfo(new EnterpriseInfo(
                "Tech Solutions S.A.C.", "Tech Solutions", "20554433221", "Av. Javier Prado 450", "contacto@tech.pe"));
    }

    @Test
    @DisplayName("Debe crear cliente personal VIP, guardar en MongoDB y emitir evento a Kafka")
    void testCreateVipPersonalCustomerSuccess() {
        Mockito.when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Customer> result = customerService.createCustomer(vipPersonalCustomer);

        StepVerifier.create(result)
                .expectNextMatches(saved -> "PERSONAL".equals(saved.getType()) &&
                        "VIP".equals(saved.getProfile()) &&
                        "ACTIVE".equals(saved.getStatus()) &&
                        saved.getCreatedAt() != null)
                .verifyComplete();

        // Verifica que el evento se publicó hacia Kafka con el perfil VIP
        Mockito.verify(eventPublisher).publish(argThat(event -> "CREATED".equals(event.eventType()) &&
                "65f1a2b3c4d5e6f7a8b9c001".equals(event.customerId()) &&
                "VIP".equals(event.profile())));
    }

    @Test
    @DisplayName("Debe crear cliente empresarial PYME y sincronizar evento hacia Kafka")
    void testCreatePymeEnterpriseCustomerSuccess() {
        Mockito.when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<Customer> result = customerService.createCustomer(pymeEnterpriseCustomer);

        StepVerifier.create(result)
                .expectNextMatches(saved -> "ENTERPRISE".equals(saved.getType()) &&
                        "PYME".equals(saved.getProfile()))
                .verifyComplete();

        Mockito.verify(eventPublisher).publish(argThat(event -> "PYME".equals(event.profile()) &&
                "ENTERPRISE".equals(event.type())));
    }

    @Test
    @DisplayName("Debe rechazar la creación si un cliente PERSONAL intenta asignarse perfil PYME")
    void testCreatePersonalCustomerWithPymeProfileFails() {
        vipPersonalCustomer.setProfile("PYME");

        StepVerifier.create(customerService.createCustomer(vipPersonalCustomer))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException &&
                        ex.getMessage().contains("perfil"))
                .verify();

        Mockito.verifyNoInteractions(eventPublisher);
    }
}
