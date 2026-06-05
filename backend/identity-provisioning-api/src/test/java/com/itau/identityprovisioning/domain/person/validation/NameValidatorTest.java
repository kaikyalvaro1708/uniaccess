package com.itau.identityprovisioning.domain.person.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// o NameValidator usa o regex ^\p{L}+(\s+\p{L}+)+$
// \p{L} aceita qualquer letra Unicode — incluindo acentos, cedilha e til
class NameValidatorTest {

    private NameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NameValidator();
    }

    // ─── nomes válidos ────────────────────────────────────────────────────────

    @Test
    @DisplayName("should accept name with two words")
    void valid_twoWords() {
        // mínimo exigido: nome + sobrenome
        assertTrue(validator.isValid("Maria Silva", null));
    }

    @Test
    @DisplayName("should accept name with three words")
    void valid_threeWords() {
        assertTrue(validator.isValid("Joao Pedro Alves", null));
    }

    @Test
    @DisplayName("should accept name with leading and trailing spaces")
    void valid_extraSpacesTrimmed() {
        // o isValid faz trim antes de aplicar o regex
        assertTrue(validator.isValid("  Maria Silva  ", null));
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
        assertTrue(validator.isValid(name, null));
    }

    @Test
    @DisplayName("should accept names with accents and cedilha")
    void valid_accentedName() {
        // \p{L} aceita letras Unicode — "João" e "Conceição" são válidos
        assertTrue(validator.isValid("João Conceição Silva", null));
    }

    // ─── nomes inválidos ──────────────────────────────────────────────────────

    @Test
    @DisplayName("should reject single-word name")
    void invalid_singleWord() {
        // o regex exige pelo menos um bloco (\s+\p{L}+) após o primeiro token
        assertFalse(validator.isValid("Maria", null));
    }

    @Test
    @DisplayName("should reject name containing digits")
    void invalid_withDigits() {
        // dígitos não são \p{L} — o regex rejeita
        assertFalse(validator.isValid("Maria123 Silva", null));
    }

    @Test
    @DisplayName("should reject name containing special characters")
    void invalid_withSpecialChars() {
        // hífen e underline não são letras Unicode — rejeitados
        for (var name : new String[]{"Maria-Silva Santos", "Maria_Silva Santos"}) {
            assertFalse(validator.isValid(name, null));
        }
    }

    @Test
    @DisplayName("should reject null name")
    void invalid_null() {
        // null é tratado explicitamente antes do regex para evitar NullPointerException
        assertFalse(validator.isValid(null, null));
    }

    @Test
    @DisplayName("should reject blank name")
    void invalid_blank() {
        assertFalse(validator.isValid("", null));
    }
}
