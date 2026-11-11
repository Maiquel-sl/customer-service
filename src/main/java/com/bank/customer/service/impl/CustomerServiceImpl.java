package com.bank.customer.service.impl;

import com.bank.customer.events.CustomerEvent;
import com.bank.customer.messaging.CustomerEventPublisher;
import com.bank.customer.model.Customer;
import com.bank.customer.model.CustomerHolder;
import com.bank.customer.repository.CustomerHolderRepository;
import com.bank.customer.repository.CustomerRepository;
import com.bank.customer.service.CustomerService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerHolderRepository holderRepository;

    private final CustomerEventPublisher eventPublisher;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerHolderRepository holderRepository,
                               CustomerEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.holderRepository = holderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Flux<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Mono<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer)
        .doOnSuccess(saved -> eventPublisher.publish(new CustomerEvent(
                    "CREATED",
                    saved.getId(),
                    saved.getType(),
                    saved.getStatus(),
                    saved.getCreatedAt()
            )));
    }

    @Override
    public Mono<Customer> updateCustomer(String id, Customer customerDetails) {
        return customerRepository.findById(id)
                .flatMap(existingCustomer -> {
                    existingCustomer.setStatus(customerDetails.getStatus());
                    existingCustomer.setPersonalInfo(customerDetails.getPersonalInfo());
                    existingCustomer.setEnterpriseInfo(customerDetails.getEnterpriseInfo());
                    existingCustomer.setSigners(customerDetails.getSigners());
                    existingCustomer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(existingCustomer)
                    .doOnSuccess(updated -> eventPublisher.publish(new CustomerEvent(
                            "UPDATED",
                            updated.getId(),
                            updated.getType(),
                            updated.getStatus(),
                            updated.getUpdatedAt()
                    )));
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String id) {
        return customerRepository.deleteById(id);
    }

    @Override
    public Mono<CustomerHolder> assignHolders(CustomerHolder holder) {
        holder.setAssignedAt(LocalDateTime.now());
        return holderRepository.save(holder);
    }

    @Override
    public Mono<CustomerHolder> getHoldersByAccountId(String accountId) {
        return holderRepository.findByAccountId(accountId);
    }
}
