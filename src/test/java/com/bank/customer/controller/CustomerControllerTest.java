package com.bank.customer.controller;

import com.bank.customer.exception.GlobalExceptionHandler;
import com.bank.customer.model.Customer;
import com.bank.customer.model.CustomerHolder;
import com.bank.customer.model.EnterpriseInfo;
import com.bank.customer.model.HolderDetail;
import com.bank.customer.model.PersonalInfo;
import com.bank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebFluxTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerService customerService;

    private Customer vipCustomer;
    private Customer pymeCustomer;

    @BeforeEach
    void setUp() {
        // Cliente Personal con perfil VIP
        vipCustomer = new Customer();
        vipCustomer.setId("65f1a2b3c4d5e6f7a8b9c001");
        vipCustomer.setType("PERSONAL");
        vipCustomer.setProfile("VIP");
        vipCustomer.setStatus("ACTIVE");
        vipCustomer.setPersonalInfo(new PersonalInfo(
                "Ana", "Gómez", "DNI", "87654321", "ana.vip@bank.com", "999111222"
        ));

        // Cliente Empresarial con perfil PYME
        pymeCustomer = new Customer();
        pymeCustomer.setId("65f1a2b3c4d5e6f7a8b9c002");
        pymeCustomer.setType("ENTERPRISE");
        pymeCustomer.setProfile("PYME");
        pymeCustomer.setStatus("ACTIVE");
        pymeCustomer.setEnterpriseInfo(new EnterpriseInfo(
                "Tech Solutions S.A.C.", "Tech Solutions","20554433221", "Av. Javier Prado 450", "contacto@tech.pe"
        ));
    }

    @Test
    @DisplayName("GET /api/customers - Debe listar todos los clientes incluyendo perfiles VIP y PYME")
    void testGetAllCustomers() {
        Mockito.when(customerService.getAllCustomers())
                .thenReturn(Flux.just(vipCustomer, pymeCustomer));

        webTestClient.get()
                .uri("/api/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].profile").isEqualTo("VIP")
                .jsonPath("$[1].profile").isEqualTo("PYME");
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Debe retornar cliente VIP cuando el ID existe")
    void testGetCustomerByIdSuccess() {
        Mockito.when(customerService.getCustomerById("65f1a2b3c4d5e6f7a8b9c001"))
                .thenReturn(Mono.just(vipCustomer));

        webTestClient.get()
                .uri("/api/customers/65f1a2b3c4d5e6f7a8b9c001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("65f1a2b3c4d5e6f7a8b9c001")
                .jsonPath("$.type").isEqualTo("PERSONAL")
                .jsonPath("$.profile").isEqualTo("VIP");
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Debe devolver 404 Not Found si el cliente no existe")
    void testGetCustomerByIdNotFound() {
        Mockito.when(customerService.getCustomerById("999999999999999999999999"))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/customers/999999999999999999999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/customers - Debe crear un cliente empresarial PYME y devolver 201 Created")
    void testCreatePymeCustomerSuccess() {
        Mockito.when(customerService.createCustomer(any(Customer.class)))
                .thenReturn(Mono.just(pymeCustomer));

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pymeCustomer)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.type").isEqualTo("ENTERPRISE")
                .jsonPath("$.profile").isEqualTo("PYME")
                .jsonPath("$.enterpriseInfo.taxId").isEqualTo("20554433221");
    }

    @Test
    @DisplayName("POST /api/customers - Debe devolver 400 Bad Request si la combinación Tipo/Perfil es inválida")
    void testCreateCustomerInvalidProfileReturnsBadRequest() {
        Mockito.when(customerService.createCustomer(any(Customer.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Un cliente PERSONAL solo admite perfil STANDARD o VIP")));

        vipCustomer.setProfile("PYME");

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(vipCustomer)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Un cliente PERSONAL solo admite perfil STANDARD o VIP");
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Debe actualizar perfil de cliente y devolver 200 OK")
    void testUpdateCustomerSuccess() {
        Mockito.when(customerService.updateCustomer(eq("65f1a2b3c4d5e6f7a8b9c001"), any(Customer.class)))
                .thenReturn(Mono.just(vipCustomer));

        webTestClient.put()
                .uri("/api/customers/65f1a2b3c4d5e6f7a8b9c001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(vipCustomer)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile").isEqualTo("VIP");
    }

    @Test
    @DisplayName("POST /api/customers/holders - Debe asignar titulares a cuenta empresarial")
    void testAssignHolders() {
        CustomerHolder holder = new CustomerHolder();
        holder.setAccountId("acc-9988");
        holder.setCustomerId("65f1a2b3c4d5e6f7a8b9c002");
        holder.setHolders(List.of(new HolderDetail("DNI", "44556677", "Marcos Pérez")));

        Mockito.when(customerService.assignHolders(any(CustomerHolder.class)))
                .thenReturn(Mono.just(holder));

        webTestClient.post()
                .uri("/api/customers/holders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(holder)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").isEqualTo("acc-9988");
    }
}
