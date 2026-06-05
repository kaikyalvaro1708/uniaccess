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

// @ExtendWith(MockitoExtension) inicializa os mocks automaticamente sem subir o Spring
// o repositório e o loginGenerator são "dublês" — não tocam no banco real
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository repository;   // simula o banco de dados

    @Mock
    private LoginGenerator loginGenerator; // simula a geração de login

    // injeta os mocks acima no PersonService como se fossem dependências reais
    @InjectMocks
    private PersonService service;

    // ─── dados de teste reutilizáveis ─────────────────────────────────────────

    // dados válidos de entrada (DTO que vem do frontend)
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

    // entidade Person já persistida (simula o que viria do banco)
    private Person samplePerson() {
        return new Person(
                1L, "Maria Silva Santos", "12345678909",
                "maria@email.com", LocalDate.of(1990, 5, 15),
                "01310100", "Avenida Paulista", "Bela Vista",
                "Sao Paulo", "SP", null,
                "mariasi", LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: should save person and return details with generated login")
    void register_success_returnsPersonDetailsWithLogin() {
        // configura os mocks: CPF não existe, login gerado é "mariasi", save devolve o objeto
        when(repository.existsByDocument("12345678909")).thenReturn(false);
        when(loginGenerator.generate(eq("Maria Silva Santos"), any())).thenReturn("mariasi");
        when(repository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.register(validData());

        // verifica que o retorno tem o login gerado e o nome correto
        assertNotNull(result);
        assertEquals("mariasi", result.login());
        assertEquals("Maria Silva Santos", result.fullName());

        // garante que o save foi chamado exatamente uma vez
        verify(repository).save(any(Person.class));
    }

    @Test
    @DisplayName("register: should throw DocumentAlreadyExistsException when document is taken")
    void register_duplicateDocument_throwsDocumentAlreadyExistsException() {
        // simula CPF já cadastrado no banco
        when(repository.existsByDocument("12345678909")).thenReturn(true);

        // deve lançar a exceção sem chegar a salvar ou gerar login
        assertThrows(DocumentAlreadyExistsException.class, () -> service.register(validData()));

        verify(repository, never()).save(any());
        verify(loginGenerator, never()).generate(any(), any());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: should return a page of PersonSummaryData")
    void findAll_returnsPageOfSummaries() {
        // retorna uma página com 1 pessoa
        var page = new PageImpl<>(List.of(samplePerson()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        var result = service.findAll(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("mariasi", result.getContent().get(0).login());
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById: should return person details when id exists")
    void findById_existing_returnsPersonDetails() {
        when(repository.findById(1L)).thenReturn(Optional.of(samplePerson()));

        var result = service.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("mariasi", result.login());
    }

    @Test
    @DisplayName("findById: should throw PersonNotFoundException when id does not exist")
    void findById_notFound_throwsPersonNotFoundException() {
        // Optional.empty() simula "nenhuma linha encontrada no banco"
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> service.findById(99L));
    }

    // ─── findByLogin ─────────────────────────────────────────────────────────
    // fluxo de login: usuário informa o login gerado e o sistema devolve os dados

    @Test
    @DisplayName("findByLogin: should return person details when login exists")
    void findByLogin_existing_returnsPersonDetails() {
        when(repository.findByLogin("mariasi")).thenReturn(Optional.of(samplePerson()));

        var result = service.findByLogin("mariasi");

        assertEquals("mariasi", result.login());
        assertEquals("Maria Silva Santos", result.fullName());
    }

    @Test
    @DisplayName("findByLogin: should throw PersonNotFoundException when login does not exist")
    void findByLogin_notFound_throwsPersonNotFoundException() {
        when(repository.findByLogin("xxxxxxx")).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> service.findByLogin("xxxxxxx"));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: should remove person when id exists")
    void delete_existing_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        // verifica que deleteById foi chamado com o id correto
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: should throw PersonNotFoundException when id does not exist")
    void delete_notFound_throwsPersonNotFoundException() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(PersonNotFoundException.class, () -> service.delete(99L));

        // garante que não tentou deletar nada quando o id não existe
        verify(repository, never()).deleteById(any());
    }
}
