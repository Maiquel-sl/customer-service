package com.bank.customer.model;

public record Signer(
    String documentType,
    String documentNumber,
    String fullName,
    String role
) {}
