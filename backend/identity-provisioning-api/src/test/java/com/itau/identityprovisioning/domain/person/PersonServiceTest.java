package com.itau.identityprovisioning.domain.person;

import com.itau.identityprovisioning.infra.exception.DocumentAlreadyExistsException;
import com.itau.identityprovisioning.infra.exception.PersonNotFoundException;
import com.itau.identityprovisioning.login.LoginGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository repository;

    @Mock
    private LoginGenerator loginGenerator;

    @InjectMocks
    private PersonService service;

    // --- test data ---

    private RegisterPersonData validData() {
        return new RegisterPersonData(
                "Maria Silva Santos",
                "123.456.789-09",
                "maria@email.com",
                LocalDate.of(1990, 5, 15),
                "01310-100",
                "Avenida Paulista",
                "Bela Vista",
                "Sao Paulo",
                "SP",
                null
        );
    }

    private Person samplePerson() {
        return new Person(
                1L, "Maria Silva Santos", "12345678909",
                "maria@email.com", LocalDate.of(1990, 5, 15),
                "01310100", "Avenida Paulista", "Bela Vista",
                "Sao Paulo", "SP", null,
                "mariasi", LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    // --- register ---

    @Test
    @DisplayName("register: should save person and return details with generated login")
    void register_success_returnsPersonDetailsWithLogin() {
        when(repository.existsByDocument("12345678909")).thenReturn(false);
        when(loginGenerator.generate(eq("Maria Silva Santos"), any())).thenReturn("mariasi");
        when(repository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.register(validData());

        System.out.printf("[cadastro] Maria Silva Santos -> login gerado: '%s' ✓%n", result.login());
        assertNotNull(result);
        assertEquals("mariasi", result.login());
        assertEquals("Maria Silva Santos", result.fullName());
        verify(repository).save(any(Person.class));
    }

    @Test
    @DisplayName("register: should throw DocumentAlreadyExistsException when document is taken")
    void register_duplicateDocument_throwsDocumentAlreadyExistsException() {
        when(repository.existsByDocument("12345678909")).thenReturn(true);

        System.out.printf("[cadastro duplicado] documento '123.456.789-09' já existe -> excecao esperada ✓%n");
        assertThrows(DocumentAlreadyExistsException.class, () -> service.register(validData()));

        verify(repository, never()).save(any());
        verify(loginGenerator, never()).generate(any(), any());
    }

    @Test
    @DisplayName("register: should strip document mask before checking uniqueness")
    void register_stripsDocumentMaskBeforeChecking() {
        when(repository.existsByDocument("12345678909")).thenReturn(false);
        when(loginGenerator.generate(any(), any())).thenReturn("mariasi");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.register(validData());

        System.out.printf("[documento limpo] '123.456.789-09' comparado como '12345678909' ✓%n");
        verify(repository).existsByDocument("12345678909");
    }

    // --- findAll ---

    @Test
    @DisplayName("findAll: should return a page of PersonSummaryData")
    void findAll_returnsPageOfSummaries() {
        var page = new PageImpl<>(List.of(samplePerson()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        var result = service.findAll(Pageable.unpaged());

        System.out.printf("[listar] %d pessoa(s) retornada(s), login: '%s' ✓%n",
                result.getTotalElements(), result.getContent().get(0).login());
        assertEquals(1, result.getTotalElements());
        assertEquals("mariasi", result.getContent().get(0).login());
    }

    // --- findById ---

    @Test
    @DisplayName("findById: should return person details when id exists")
    void findById_existing_returnsPersonDetails() {
        when(repository.findById(1L)).thenReturn(Optional.of(samplePerson()));

        var result = service.findById(1L);

        System.out.printf("[buscar por id] id=1 -> '%s' (login: %s) ✓%n", result.fullName(), result.login());
        assertEquals(1L, result.id());
        assertEquals("mariasi", result.login());
    }

    @Test
    @DisplayName("findById: should throw PersonNotFoundException when id does not exist")
    void findById_notFound_throwsPersonNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        System.out.printf("[buscar por id] id=99 nao existe -> excecao esperada ✓%n");
        assertThrows(PersonNotFoundException.class, () -> service.findById(99L));
    }

    // --- delete ---

    @Test
    @DisplayName("delete: should remove person when id exists")
    void delete_existing_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        System.out.printf("[deletar] id=1 removido com sucesso ✓%n");
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: should throw PersonNotFoundException when id does not exist")
    void delete_notFound_throwsPersonNotFoundException() {
        when(repository.existsById(99L)).thenReturn(false);

        System.out.printf("[deletar] id=99 nao existe -> excecao esperada ✓%n");
        assertThrows(PersonNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any());
    }
}
