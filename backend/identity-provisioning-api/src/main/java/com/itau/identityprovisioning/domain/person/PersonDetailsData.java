package com.itau.identityprovisioning.domain.person;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PersonDetailsData(
    Long id,
    String fullName,
    String document,
    String email,
    LocalDate dateOfBirth,
    String zipCode,
    String street,
    String neighborhood,
    String city,
    String state,
    String complement,
    String login,
    LocalDateTime createdAt
) {
    public PersonDetailsData(Person person) {
        this(
            person.getId(),
            person.getFullName(),
            formatDocument(person.getDocument()),
            person.getEmail(),
            person.getDateOfBirth(),
            person.getZipCode(),
            person.getStreet(),
            person.getNeighborhood(),
            person.getCity(),
            person.getState(),
            person.getComplement(),
            person.getLogin(),
            person.getCreatedAt()
        );
    }

    // returns CPF formatted as 999.999.999-99
    private static String formatDocument(String document) {
        if (document == null || document.length() != 11) return document;
        return document.substring(0, 3) + "." +
               document.substring(3, 6) + "." +
               document.substring(6, 9) + "-" +
               document.substring(9);
    }
}
