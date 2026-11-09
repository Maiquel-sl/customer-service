package com.bank.customer.repository;

import com.bank.customer.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Mono<Customer> findByPersonalInfoDocumentNumber(String documentNumber);
    Mono<Customer> findByEnterpriseInfoTaxId(String taxId);
}