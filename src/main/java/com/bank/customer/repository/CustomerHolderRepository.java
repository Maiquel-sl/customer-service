package com.bank.customer.repository;

import com.bank.customer.model.CustomerHolder;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CustomerHolderRepository extends ReactiveMongoRepository<CustomerHolder, String> {
    Mono<CustomerHolder> findByAccountId(String accountId);
}
