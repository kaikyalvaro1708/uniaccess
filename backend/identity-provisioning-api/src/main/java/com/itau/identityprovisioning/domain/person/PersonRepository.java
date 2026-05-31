package com.itau.identityprovisioning.domain.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByDocument(String document);
    boolean existsByLogin(String login);
    Optional<Person> findByLogin(String login);
    Optional<Person> findByEmail(String email);
}
