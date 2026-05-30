package com.itau.identityprovisioning.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginGeneratorTest {

    private LoginGenerator generator;
    private Set<String> existingLogins;
    private LoginAvailabilityChecker seedChecker;

    private static final LoginAvailabilityChecker ACCEPT_ALL = login -> true;

    @BeforeEach
    void setUp() throws IOException {
        generator      = new LoginGenerator();
        existingLogins = loadLoginsFromMassData();
        seedChecker    = login -> !existingLogins.contains(login);
    }

    // --- happy path ---

    @Test
    @DisplayName("should return first deterministic candidate when no collision")
    void happyPath_noCollision_returnsFirstCandidate() {
        var login = generator.generate("Joana Dark Souza", ACCEPT_ALL);
        System.out.printf("[generate] Joana Dark Souza -> %s%n", login);
        assertEquals("joanada", login);
    }

    @Test
    @DisplayName("should return mariasi for Maria Silva Santos")
    void happyPath_mariaSilvaSantos() {
        var login = generator.generate("Maria Silva Santos", ACCEPT_ALL);
        System.out.printf("[generate] Maria Silva Santos -> %s%n", login);
        assertEquals("mariasi", login);
    }

    @Test
    @DisplayName("should return joaoped for Joao Pedro Alves")
    void happyPath_joaoPedroAlves() {
        var login = generator.generate("Joao Pedro Alves", ACCEPT_ALL);
        System.out.printf("[generate] Joao Pedro Alves -> %s%n", login);
        assertEquals("joaoped", login);
    }

    @Test
    @DisplayName("should return anaclar for Ana Clara Souza")
    void happyPath_anaClara() {
        var login = generator.generate("Ana Clara Souza", ACCEPT_ALL);
        System.out.printf("[generate] Ana Clara Souza -> %s%n", login);
        assertEquals("anaclar", login);
    }

    @Test
    @DisplayName("should return carlose for Carlos Eduardo Lima")
    void happyPath_carlosEduardo() {
        var login = generator.generate("Carlos Eduardo Lima", ACCEPT_ALL);
        System.out.printf("[generate] Carlos Eduardo Lima -> %s%n", login);
        assertEquals("carlose", login);
    }

    // --- collision with seed data ---

    @Test
    @DisplayName("should skip mariasi when taken and return a different valid login")
    void collision_mariaSilvaSantos_returnsAlternative() {
        var login = generator.generate("Maria Silva Santos", seedChecker);
        System.out.printf("[collision] Maria Silva Santos (seed) -> %s%n", login);
        assertValidLogin(login);
        assertFalse(existingLogins.contains(login));
    }

    @Test
    @DisplayName("should skip all four maria* logins when taken")
    void collision_allMariasInSeedTaken() {
        var extended = new HashSet<>(existingLogins);
        extended.add("mariasi");
        extended.add("mariasa");
        extended.add("mariass");
        extended.add("mariasl");

        var login = generator.generate("Maria Sousa Neves", candidate -> !extended.contains(candidate));
        System.out.printf("[collision] Maria Sousa Neves (all maria* taken) -> %s%n", login);
        assertValidLogin(login);
        assertFalse(extended.contains(login));
    }

    @Test
    @DisplayName("should skip joaoped when taken and return a different valid login")
    void collision_joaoPedroAlves_returnsAlternative() {
        var login = generator.generate("Joao Pedro Alves", seedChecker);
        System.out.printf("[collision] Joao Pedro Alves (seed) -> %s%n", login);
        assertValidLogin(login);
        assertFalse(existingLogins.contains(login));
    }

    // --- accent and cedilha normalisation ---

    @Test
    @DisplayName("should strip accented vowels — result is plain a-z")
    void normalisation_accentedVowels() {
        var login = generator.generate("José Álvaro Pereira", ACCEPT_ALL);
        System.out.printf("[normalise] José Álvaro Pereira -> %s%n", login);
        assertValidLogin(login);
    }

    @Test
    @DisplayName("should normalise cedilha ç -> c")
    void normalisation_cedilha() {
        var login = generator.generate("Francisco Conceição Lima", ACCEPT_ALL);
        System.out.printf("[normalise] Francisco Conceição Lima -> %s%n", login);
        assertValidLogin(login);
    }

    @Test
    @DisplayName("should handle full accent mix — ã â é ê ó ô ú")
    void normalisation_fullMix() {
        var login = generator.generate("Renânia Ângelo Ôsvaldo", ACCEPT_ALL);
        System.out.printf("[normalise] Renânia Ângelo Ôsvaldo -> %s%n", login);
        assertValidLogin(login);
    }

    // --- short names ---

    @Test
    @DisplayName("should pad to 7 chars when name has fewer than 7 useful letters")
    void shortName_fewerThan7Letters() {
        var login = generator.generate("Ana Bo", ACCEPT_ALL);
        System.out.printf("[short] Ana Bo -> %s%n", login);
        assertValidLogin(login);
    }

    @Test
    @DisplayName("should produce a valid login even for a single-token name")
    void shortName_singleToken() {
        var login = generator.generate("Lia", ACCEPT_ALL);
        System.out.printf("[short] Lia -> %s%n", login);
        assertValidLogin(login);
    }

    // --- normalise helper unit tests ---

    @Test
    @DisplayName("normalize should strip accents and return clean tokens")
    void normalize_stripsAccentsAndSplits() {
        var tokens = generator.normalize("José Álvaro Conceição");
        System.out.printf("[normalize] José Álvaro Conceição -> %s | %s | %s%n",
                tokens[0], tokens[1], tokens[2]);
        assertEquals("jose",      tokens[0]);
        assertEquals("alvaro",    tokens[1]);
        assertEquals("conceicao", tokens[2]);
    }

    @Test
    @DisplayName("normalize should remove digits and special symbols")
    void normalize_removesDigitsAndSymbols() {
        var tokens = generator.normalize("Joao123 Pedro@Antunes!");
        System.out.printf("[normalize] Joao123 Pedro@Antunes! -> %s | %s | %s%n",
                tokens[0], tokens[1], tokens[2]);
        assertEquals("joao",    tokens[0]);
        assertEquals("pedro",   tokens[1]);
        assertEquals("antunes", tokens[2]);
    }

    // --- invariants ---

    @ParameterizedTest(name = "{0}")
    @DisplayName("login must always be exactly 7 chars")
    @ValueSource(strings = {
            "Maria Santos",
            "Carlos Eduardo Lima",
            "Jo Bo",
            "Lucas Henrique Prado",
            "Ana"
    })
    void invariant_alwaysExactly7Chars(String name) {
        var login = generator.generate(name, ACCEPT_ALL);
        System.out.printf("[invariant] %-28s -> %s (length=%d)%n", name, login, login.length());
        assertEquals(7, login.length());
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("login must always match [a-z]{7}")
    @ValueSource(strings = {
            "Maria Santos",
            "José Álvaro Pereira",
            "François Dupont",
            "Conceição Barros"
    })
    void invariant_alwaysLowercaseAZ(String name) {
        var login = generator.generate(name, ACCEPT_ALL);
        System.out.printf("[invariant] %-28s -> %s%n", name, login);
        assertTrue(login.matches("[a-z]{7}"));
    }

    @Test
    @DisplayName("login must never contain a digit even when name has digits")
    void invariant_noDigits() {
        var login = generator.generate("Joao Pedro123", ACCEPT_ALL);
        System.out.printf("[invariant] Joao Pedro123 -> %s%n", login);
        assertFalse(login.chars().anyMatch(Character::isDigit));
        assertValidLogin(login);
    }

    @Test
    @DisplayName("login must never contain uppercase letters")
    void invariant_noUppercase() {
        var login = generator.generate("CARLOS SOUZA", ACCEPT_ALL);
        System.out.printf("[invariant] CARLOS SOUZA -> %s%n", login);
        assertEquals(login.toLowerCase(), login);
        assertValidLogin(login);
    }

    // --- helpers ---

    private void assertValidLogin(String login) {
        assertNotNull(login);
        assertEquals(7, login.length(), "expected 7 chars, got: " + login);
        assertTrue(login.matches("[a-z]{7}"), "expected [a-z]{7}, got: " + login);
    }

    private static Set<String> loadLoginsFromMassData() throws IOException {
        var logins = new HashSet<String>();
        InputStream is = LoginGeneratorTest.class.getClassLoader()
                .getResourceAsStream("massa_dados.txt");

        assertNotNull(is, "massa_dados.txt not found on classpath");

        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                var parts = line.split("\\|");
                if (parts.length >= 5) logins.add(parts[4].trim());
            }
        }
        return logins;
    }
}
