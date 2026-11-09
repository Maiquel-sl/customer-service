package com.bank.customer.model;

public record EnterpriseInfo(
    String businessName,
    String commercialName,
    String taxId,
    String email,
    String phone
) {}
