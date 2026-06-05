package com.itau.identityprovisioning.controller;

import com.itau.identityprovisioning.domain.person.PersonDetailsData;
import com.itau.identityprovisioning.domain.person.PersonService;
import com.itau.identityprovisioning.domain.person.PersonSummaryData;
import com.itau.identityprovisioning.infra.exception.DocumentAlreadyExistsException;
import com.itau.identityprovisioning.infra.exception.PersonNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService service;

    // --- test data ---

    private PersonDetailsData sampleDetails() {
        return new PersonDetailsData(
                1L, "Maria Silva Santos", "123.456.789-09",
                "maria@email.com", LocalDate.of(1990, 5, 15),
                "01310100", "Avenida Paulista", "Bela Vista",
                "Sao Paulo", "SP", null,
                "mariasi", LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    private PersonSummaryData sampleSummary() {
        return new PersonSummaryData(
                1L, "Maria Silva Santos", "maria@email.com",
                "mariasi", LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    private String validRequestBody() {
        return """
                {
                  "fullName": "Maria Silva Santos",
                  "document": "123.456.789-09",
                  "email": "maria@email.com",
                  "dateOfBirth": "1990-05-15",
                  "zipCode": "01310-100",
                  "street": "Avenida Paulista",
                  "neighborhood": "Bela Vista",
                  "city": "Sao Paulo",
                  "state": "SP"
                }
                """;
    }

    // --- POST /api/persons ---

    @Test
    @DisplayName("POST /api/persons: should return 201 with generated login and Location header")
    void register_validRequest_returns201WithLoginAndLocation() throws Exception {
        when(service.register(any())).thenReturn(sampleDetails());

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("mariasi"))
                .andExpect(jsonPath("$.fullName").value("Maria Silva Santos"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /api/persons: should return 400 when required fields are missing")
    void register_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fullName": "Maria Silva Santos" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/persons: should return 409 when document already exists")
    void register_duplicateDocument_returns409() throws Exception {
        when(service.register(any())).thenThrow(new DocumentAlreadyExistsException());

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    // --- GET /api/persons ---

    @Test
    @DisplayName("GET /api/persons: should return 200 with paginated list")
    void list_returns200WithPagedContent() throws Exception {
        var page = new PageImpl<>(List.of(sampleSummary()));
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].login").value("mariasi"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // --- GET /api/persons/{id} ---

    @Test
    @DisplayName("GET /api/persons/{id}: should return 200 with full details")
    void findById_existing_returns200WithDetails() throws Exception {
        when(service.findById(1L)).thenReturn(sampleDetails());

        mockMvc.perform(get("/api/persons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("mariasi"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    @DisplayName("GET /api/persons/{id}: should return 404 when person does not exist")
    void findById_notFound_returns404() throws Exception {
        when(service.findById(99L)).thenThrow(new PersonNotFoundException(99L));

        mockMvc.perform(get("/api/persons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Person not found"));
    }

    // --- GET /api/persons/login/{login} ---

    // testa o fluxo de login: endpoint que o frontend chama quando o usuário digita o login
    @Test
    @DisplayName("GET /api/persons/login/{login}: should return 200 with person details")
    void findByLogin_existing_returns200() throws Exception {
        when(service.findByLogin("mariasi")).thenReturn(sampleDetails());

        mockMvc.perform(get("/api/persons/login/mariasi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("mariasi"))
                .andExpect(jsonPath("$.fullName").value("Maria Silva Santos"));
    }

    @Test
    @DisplayName("GET /api/persons/login/{login}: should return 404 when login does not exist")
    void findByLogin_notFound_returns404() throws Exception {
        when(service.findByLogin("xxxxxxx")).thenThrow(new PersonNotFoundException(0L));

        mockMvc.perform(get("/api/persons/login/xxxxxxx"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/persons/{id} ---

    @Test
    @DisplayName("DELETE /api/persons/{id}: should return 204 when person exists")
    void delete_existing_returns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/persons/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/persons/{id}: should return 404 when person does not exist")
    void delete_notFound_returns404() throws Exception {
        doThrow(new PersonNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/persons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Person not found"));
    }
}
