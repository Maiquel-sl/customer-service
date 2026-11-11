package com.bank.customer.service.impl;

import com.bank.customer.events.CustomerEvent;
import com.bank.customer.messaging.CustomerEventPublisher;
import com.bank.customer.model.Customer;
import com.bank.customer.model.CustomerHolder;
import com.bank.customer.repository.CustomerHolderRepository;
import com.bank.customer.repository.CustomerRepository;
import com.bank.customer.service.CustomerService;
import com.bank.customer.util.ReactiveFallbackHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

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
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericFluxFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Flux<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<Customer> createCustomer(Customer customer) {
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        // Flujo reactivo original (si tarda más de 2s, salta al fallback)
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
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
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
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<Void> deleteCustomer(String id) {
        return customerRepository.deleteById(id);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<CustomerHolder> assignHolders(CustomerHolder holder) {
        holder.setAssignedAt(LocalDateTime.now());
        return holderRepository.save(holder);
    }

    @Override
    @CircuitBreaker(name = "bankServiceCB", fallbackMethod = "genericMonoFallback")
    @TimeLimiter(name = "bankServiceCB")
    public Mono<CustomerHolder> getHoldersByAccountId(String accountId) {
        return holderRepository.findByAccountId(accountId);
    }

    // =========================================================================
    // MÉTODOS FALLBACK GENÉRICOS PARA TODA LA CLASE
    // =========================================================================

    @SuppressWarnings("unused")
    private <T> Mono<T> genericMonoFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleMonoFallback("TransactionService", ex);
    }

    @SuppressWarnings("unused")
    private <T> Flux<T> genericFluxFallback(Throwable ex) {
        return ReactiveFallbackHandler.handleFluxFallback("TransactionService", ex);
    }
}
