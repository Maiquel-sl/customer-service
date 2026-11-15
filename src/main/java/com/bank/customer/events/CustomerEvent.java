package com.bank.customer.events;

import java.time.LocalDateTime;

public record CustomerEvent(
    String eventType,   // CREATED, UPDATED
    String customerId,
    String type,        // PERSONAL, ENTERPRISE
    String profile,     // STANDARD, VIP, PYME
    String status,      // ACTIVE, INACTIVE
    LocalDateTime timestamp
) {}
