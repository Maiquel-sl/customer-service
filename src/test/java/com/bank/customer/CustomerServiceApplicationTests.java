package com.bank.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerServiceApplicationTests {

    @Test
    @DisplayName("Should verify that the reactive application context loads successfully")
    void contextLoads() {
        // This test will fail if any bean definition, database connection configuration, 
        // or reactive pipeline initialization is misconfigured.
    }
}