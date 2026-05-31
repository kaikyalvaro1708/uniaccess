package com.itau.identityprovisioning.domain.person.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameValidatorTest {

    private NameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NameValidator();
    }

    // --- valid names ---

    @Test
    @DisplayName("should accept name with two words")
    void valid_twoWords() {
        var name = "Maria Silva";
        var result = validator.isValid(name, null);
        System.out.printf("[Nome válido - 2 palavras]   '%s' -> %s%n", name, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    @Test
    @DisplayName("should accept name with three words")
    void valid_threeWords() {
        var name = "Joao Pedro Alves";
        var result = validator.isValid(name, null);
        System.out.printf("[Nome válido - 3 palavras]   '%s' -> %s%n", name, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    @Test
    @DisplayName("should accept name with leading and trailing spaces")
    void valid_extraSpacesTrimmed() {
        var name = "  Maria Silva  ";
        var result = validator.isValid(name, null);
        System.out.printf("[Nome válido - espaços extra] '%s' -> %s%n", name.trim(), result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    // --- invalid names ---

    @Test
    @DisplayName("should reject single-word name")
    void invalid_singleWord() {
        var name = "Maria";
        var result = validator.isValid(name, null);
        System.out.printf("[Nome inválido - 1 palavra]  '%s' -> %s%n", name, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @ParameterizedTest
    @DisplayName("should accept plain ASCII names without accents")
    @ValueSource(strings = {
            "Jose Silva",
            "Joao Pedro",
            "Conceicao Lima",
            "Francois Dupont"
    })
    void valid_plainAsciiNames(String name) {
        var result = validator.isValid(name, null);
        System.out.printf("[Nome sem acento]            '%s' -> %s%n", name, result ? "ACEITO ✓" : "REJEITADO ✗");
        assertTrue(result);
    }

    @Test
    @DisplayName("should reject name containing digits")
    void invalid_withDigits() {
        var name = "Maria123 Silva";
        var result = validator.isValid(name, null);
        System.out.printf("[Nome inválido - dígitos]    '%s' -> %s%n", name, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @Test
    @DisplayName("should reject name containing special characters")
    void invalid_withSpecialChars() {
        for (var name : new String[]{"Maria-Silva Santos", "Maria_Silva Santos"}) {
            var result = validator.isValid(name, null);
            System.out.printf("[Nome inválido - especiais]  '%s' -> %s%n", name, !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("should reject null name")
    void invalid_null() {
        var result = validator.isValid(null, null);
        System.out.printf("[Nome inválido - nulo]       null -> %s%n", !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }

    @Test
    @DisplayName("should reject blank name")
    void invalid_blank() {
        var result = validator.isValid("", null);
        System.out.printf("[Nome inválido - branco]     \"\" -> %s%n", !result ? "REJEITADO corretamente ✓" : "ACEITO incorretamente ✗");
        assertFalse(result);
    }
}
