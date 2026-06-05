package com.itau.identityprovisioning.domain.person.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    private CpfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfValidator();
    }

    // ─── CPFs válidos ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("should accept valid CPF with mask")
    void valid_withMask() {
        // o validator remove a máscara antes de calcular — deve aceitar com e sem
        assertTrue(validator.isValid("123.456.789-09", null));
    }

    @ParameterizedTest
    @DisplayName("should accept known valid CPFs")
    @ValueSource(strings = {
            "987.654.321-00",
            "111.444.777-35",
            "529.982.247-25"
    })
    void valid_knownCpfs(String cpf) {
        // CPFs com dígito verificador correto, calculados pelo algoritmo da Receita Federal
        assertTrue(validator.isValid(cpf, null));
    }

    // ─── CPFs inválidos ───────────────────────────────────────────────────────

    @Test
    @DisplayName("should reject CPF with wrong check digit")
    void invalid_wrongCheckDigit() {
        // último dígito errado — o algoritmo calcula e detecta a inconsistência
        assertFalse(validator.isValid("123.456.789-00", null));
    }

    @Test
    @DisplayName("should reject CPF with all same digits")
    void invalid_allSameDigits() {
        // CPFs como 111.111.111-11 são matematicamente válidos pelo algoritmo
        // mas a Receita Federal os rejeita — temos uma guarda explícita para isso
        for (var cpf : new String[]{"111.111.111-11", "000.000.000-00", "999.999.999-99"}) {
            assertFalse(validator.isValid(cpf, null));
        }
    }

    @Test
    @DisplayName("should reject null CPF")
    void invalid_null() {
        // null deve retornar false sem lançar NullPointerException
        assertFalse(validator.isValid(null, null));
    }

    @Test
    @DisplayName("should reject blank CPF")
    void invalid_blank() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    @DisplayName("should reject CPF with wrong length")
    void invalid_wrongLength() {
        // após remover a máscara, o CPF deve ter exatamente 11 dígitos
        for (var cpf : new String[]{"123.456.789", "123.456.789-091"}) {
            assertFalse(validator.isValid(cpf, null));
        }
    }
}
