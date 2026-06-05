package com.itau.identityprovisioning.domain.person;

import java.time.LocalDateTime;

public record PersonSummaryData(
        Long id,
        String fullName,
        String email,
        String login,
        LocalDateTime createdAt
) {
    public PersonSummaryData(Person person) {
        this(
                person.getId(),
                person.getFullName(),
                person.getEmail(),
                person.getLogin(),
                person.getCreatedAt()
        );
    }
}
