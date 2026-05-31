package com.itau.identityprovisioning.domain.person;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByDocument(String document);
    boolean existsByLogin(String login);
}
