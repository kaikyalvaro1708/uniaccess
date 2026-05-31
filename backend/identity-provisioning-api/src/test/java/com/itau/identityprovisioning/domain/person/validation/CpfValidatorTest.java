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

    // --- valid CPFs ---

    @Test
    @DisplayName("should accept valid CPF with mask")
    void valid_withMask() {
        var cpf = "123.456.789-09";
        var result = validator.isValid(cpf, null);
        System.out.printf("[CPF válido com máscara]  %s -> %s%n", cpf, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    @Test
    @DisplayName("should accept valid CPF without mask")
    void valid_withoutMask() {
        var cpf = "12345678909";
        var result = validator.isValid(cpf, null);
        System.out.printf("[CPF válido sem máscara]  %s -> %s%n", cpf, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    @ParameterizedTest
    @DisplayName("should accept known valid CPFs")
    @ValueSource(strings = {
            "987.654.321-00",
            "111.444.777-35",
            "529.982.247-25"
    })
    void valid_knownCpfs(String cpf) {
        var result = validator.isValid(cpf, null);
        System.out.printf("[CPF válido conhecido]    %s -> %s%n", cpf, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    // --- invalid CPFs ---

    @Test
    @DisplayName("should reject CPF with wrong check digit")
    void invalid_wrongCheckDigit() {
        var cpf = "123.456.789-00";
        var result = validator.isValid(cpf, null);
        System.out.printf("[CPF dígito errado]       %s -> %s%n", cpf, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @Test
    @DisplayName("should reject CPF with all same digits")
    void invalid_allSameDigits() {
        for (var cpf : new String[]{"111.111.111-11", "000.000.000-00", "999.999.999-99"}) {
            var result = validator.isValid(cpf, null);
            System.out.printf("[CPF dígitos iguais]      %s -> %s%n", cpf, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("should reject null CPF")
    void invalid_null() {
        var result = validator.isValid(null, null);
        System.out.printf("[CPF nulo]                null -> %s%n", !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @Test
    @DisplayName("should reject blank CPF")
    void invalid_blank() {
        var result = validator.isValid("", null);
        System.out.printf("[CPF em branco]           \"\" -> %s%n", !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @Test
    @DisplayName("should reject CPF with wrong length")
    void invalid_wrongLength() {
        for (var cpf : new String[]{"123.456.789", "123.456.789-091"}) {
            var result = validator.isValid(cpf, null);
            System.out.printf("[CPF tamanho errado]      %s -> %s%n", cpf, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
            assertFalse(result);
        }
    }
}
