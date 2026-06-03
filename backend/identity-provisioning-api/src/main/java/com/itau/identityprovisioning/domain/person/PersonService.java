package com.itau.identityprovisioning.domain.person;

import com.itau.identityprovisioning.infra.exception.DocumentAlreadyExistsException;
import com.itau.identityprovisioning.infra.exception.PersonNotFoundException;
import com.itau.identityprovisioning.login.LoginGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    @Autowired
    private PersonRepository repository;

    @Autowired
    private LoginGenerator loginGenerator;

    public PersonDetailsData register(RegisterPersonData data) {
        var cleanDocument = data.document().replaceAll("[^0-9]", "");

        if (repository.existsByDocument(cleanDocument)) {
            log.warn("Registration attempt with duplicate document: {}", maskDocument(cleanDocument));
            throw new DocumentAlreadyExistsException();
        }

        var login = loginGenerator.generate(
                data.fullName(),
                candidate -> !repository.existsByLogin(candidate)
        );

        var person = new Person(data, login);
        repository.save(person);

        log.info("Person registered with login: {}", login);
        return new PersonDetailsData(person);
    }

    public Page<PersonSummaryData> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(PersonSummaryData::new);
    }

    public PersonDetailsData findById(Long id) {
        var person = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
        return new PersonDetailsData(person);
    }

    public PersonDetailsData findByLogin(String login) {
        var person = repository.findByLogin(login)
                .orElseThrow(() -> new PersonNotFoundException(0L));
        return new PersonDetailsData(person);
    }

    public PersonDetailsData findByEmail(String email) {
        var person = repository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new PersonNotFoundException(0L));
        return new PersonDetailsData(person);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PersonNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Person deleted with id: {}", id);
    }

    // masks CPF for safe logging: 12345678901 -> 123.***.**9-01
    private String maskDocument(String digits) {
        if (digits.length() != 11) return "***";
        return digits.substring(0, 3) + ".***.**" + digits.charAt(8) + "-" + digits.substring(9);
    }
}
