package com.bank.customer.messaging;

import com.bank.customer.events.CustomerEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.function.Supplier;

@Component
public class CustomerEventPublisher {

    private final Sinks.Many<CustomerEvent> sink = 
            Sinks.many().multicast().onBackpressureBuffer();

    public void publish(CustomerEvent event) {
        sink.tryEmitNext(event);
    }

    @Bean
    public Supplier<Flux<CustomerEvent>> customerEventProducer() {
        return sink::asFlux;
    }
}
