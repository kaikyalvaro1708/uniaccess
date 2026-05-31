package com.itau.identityprovisioning.domain.zipcode;

public record ZipCodeDetails(
    String zipCode,
    String street,
    String neighborhood,
    String city,
    String state
) {}
