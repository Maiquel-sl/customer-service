package com.bank.customer.service;

import com.bank.customer.model.Customer;
import com.bank.customer.model.CustomerHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Flux<Customer> getAllCustomers();
    Mono<Customer> getCustomerById(String id);
    Mono<Customer> createCustomer(Customer customer);
    Mono<Customer> updateCustomer(String id, Customer customer);
    Mono<Void> deleteCustomer(String id);

    Mono<CustomerHolder> assignHolders(CustomerHolder holder);
    Mono<CustomerHolder> getHoldersByAccountId(String accountId);
}
