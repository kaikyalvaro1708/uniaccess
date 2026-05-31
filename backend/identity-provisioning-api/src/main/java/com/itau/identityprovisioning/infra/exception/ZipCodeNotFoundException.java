package com.itau.identityprovisioning.infra.exception;

public class ZipCodeNotFoundException extends RuntimeException {
    public ZipCodeNotFoundException(String zipCode) {
        super("Zip code not found: " + zipCode);
    }
}
