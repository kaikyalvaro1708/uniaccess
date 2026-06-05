package com.itau.identityprovisioning.infra.exception;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(Long id) {
        super("Person not found with id: " + id);
    }

    public PersonNotFoundException(String field, String value) {
        super("Person not found with " + field + ": " + value);
    }
}
