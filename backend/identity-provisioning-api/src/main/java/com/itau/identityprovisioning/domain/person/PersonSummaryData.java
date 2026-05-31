package com.itau.identityprovisioning.domain.person;

import java.time.LocalDateTime;

// lightweight projection used in list responses — avoids sending full address details for every item
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
