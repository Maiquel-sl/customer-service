package com.bank.customer.model;

public record PersonalInfo(
    String firstName,
    String lastName,
    String documentType,
    String documentNumber,
    String email,
    String phone
) {}
