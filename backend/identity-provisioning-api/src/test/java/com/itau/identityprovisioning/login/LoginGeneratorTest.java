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

    // logins já existentes carregados da massa de dados legada (V2 migration)
    private Set<String> existingLogins;

    // checker que simula o banco real: retorna disponível apenas se o login não estiver na massa
    private LoginAvailabilityChecker seedChecker;

    // checker que sempre aprova — usado quando não queremos simular colisão
    private static final LoginAvailabilityChecker ACCEPT_ALL = login -> true;

    @BeforeEach
    void setUp() throws IOException {
        generator      = new LoginGenerator();
        existingLogins = loadLoginsFromMassData();
        seedChecker    = login -> !existingLogins.contains(login);
    }

    // ─── happy path ──────────────────────────────────────────────────────────
    // verifica que o primeiro candidato gerado bate com o esperado pelo algoritmo

    @Test
    @DisplayName("should return mariasi for Maria Silva Santos")
    void happyPath_mariaSilvaSantos() {
        // "maria" (5) + "si" (2 letras de Silva) = "mariasi"
        var login = generator.generate("Maria Silva Santos", ACCEPT_ALL);
        assertEquals("mariasi", login);
    }

    @Test
    @DisplayName("should return joaoped for Joao Pedro Alves")
    void happyPath_joaoPedroAlves() {
        // "joao" (4) + "ped" (3 letras de Pedro) = "joaoped"
        var login = generator.generate("Joao Pedro Alves", ACCEPT_ALL);
        assertEquals("joaoped", login);
    }

    @Test
    @DisplayName("should return anaclar for Ana Clara Souza")
    void happyPath_anaClara() {
        // "ana" (3) + "clar" (4 letras de Clara) = "anaclar"
        var login = generator.generate("Ana Clara Souza", ACCEPT_ALL);
        assertEquals("anaclar", login);
    }

    @Test
    @DisplayName("should return carlose for Carlos Eduardo Lima")
    void happyPath_carlosEduardo() {
        // "carlos" (6) + "e" (1 letra de Eduardo) = "carlose"
        var login = generator.generate("Carlos Eduardo Lima", ACCEPT_ALL);
        assertEquals("carlose", login);
    }

    // ─── colisão com dados legados ───────────────────────────────────────────
    // o seedChecker simula que os logins da massa já estão ocupados no banco
    // o gerador precisa encontrar uma alternativa válida

    @Test
    @DisplayName("should skip mariasi when taken and return a different valid login")
    void collision_mariaSilvaSantos_returnsAlternative() {
        // "mariasi" já existe na massa — o gerador deve desviar e retornar outro login
        var login = generator.generate("Maria Silva Santos", seedChecker);
        assertValidLogin(login);
        assertFalse(existingLogins.contains(login), "login gerado não pode estar na massa legada");
    }

    @Test
    @DisplayName("should skip joaoped when taken and return a different valid login")
    void collision_joaoPedroAlves_returnsAlternative() {
        var login = generator.generate("Joao Pedro Alves", seedChecker);
        assertValidLogin(login);
        assertFalse(existingLogins.contains(login));
    }

    // ─── normalização de acentos e cedilha ───────────────────────────────────
    // o algoritmo usa NFD para remover acentos antes de gerar o login
    // resultado final deve conter apenas [a-z]

    @Test
    @DisplayName("should strip accented vowels — result is plain a-z")
    void normalisation_accentedVowels() {
        // "José" → "jose", "Álvaro" → "alvaro" — todos os acentos removidos
        var login = generator.generate("José Álvaro Pereira", ACCEPT_ALL);
        assertValidLogin(login);
    }

    @Test
    @DisplayName("should normalise cedilha ç -> c")
    void normalisation_cedilha() {
        // "Conceição" → "conceicao" — cedilha vira c, til vira a
        var login = generator.generate("Francisco Conceição Lima", ACCEPT_ALL);
        assertValidLogin(login);
    }

    // ─── nomes curtos ─────────────────────────────────────────────────────────
    // quando o nome tem menos de 7 letras úteis o algoritmo preenche com 'a'

    @Test
    @DisplayName("should pad to 7 chars when name has fewer than 7 useful letters")
    void shortName_fewerThan7Letters() {
        // "ana" + "bo" = 5 letras → algoritmo completa com 'a' até 7: "anaboaa"
        var login = generator.generate("Ana Bo", ACCEPT_ALL);
        assertValidLogin(login);
    }

    // ─── testes do método normalize() diretamente ────────────────────────────
    // normalize() é package-private — testamos para garantir a tokenização correta

    @Test
    @DisplayName("normalize should strip accents and return clean tokens")
    void normalize_stripsAccentsAndSplits() {
        // "José Álvaro Conceição" deve virar 3 tokens: ["jose", "alvaro", "conceicao"]
        var tokens = generator.normalize("José Álvaro Conceição");
        assertEquals("jose",      tokens[0]);
        assertEquals("alvaro",    tokens[1]);
        assertEquals("conceicao", tokens[2]);
    }

    // ─── invariantes — regras que NUNCA podem ser violadas ───────────────────

    @ParameterizedTest(name = "{0}")
    @DisplayName("login must always be exactly 7 chars")
    @ValueSource(strings = {
            "Maria Santos",
            "Carlos Eduardo Lima",
            "Jo Bo"
    })
    void invariant_alwaysExactly7Chars(String name) {
        // independente do nome, o login sempre deve ter exatamente 7 caracteres
        var login = generator.generate(name, ACCEPT_ALL);
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
        // o login só pode conter letras minúsculas de a a z — sem acento, sem número
        var login = generator.generate(name, ACCEPT_ALL);
        assertTrue(login.matches("[a-z]{7}"));
    }

    @Test
    @DisplayName("login must never contain a digit even when name has digits")
    void invariant_noDigits() {
        // dígitos no nome são ignorados na normalização — o login não pode ter número
        var login = generator.generate("Joao Pedro123", ACCEPT_ALL);
        assertFalse(login.chars().anyMatch(Character::isDigit));
        assertValidLogin(login);
    }

    @Test
    @DisplayName("login must never contain uppercase letters")
    void invariant_noUppercase() {
        // nome em maiúsculas deve ser normalizado para minúsculas
        var login = generator.generate("CARLOS SOUZA", ACCEPT_ALL);
        assertEquals(login.toLowerCase(), login);
        assertValidLogin(login);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    // valida as 3 regras obrigatórias do login: não nulo, exatamente 7 chars, só [a-z]
    private void assertValidLogin(String login) {
        assertNotNull(login);
        assertEquals(7, login.length(), "esperado 7 chars, recebido: " + login);
        assertTrue(login.matches("[a-z]{7}"), "esperado [a-z]{7}, recebido: " + login);
    }

    // lê os logins já existentes do arquivo massa_dados.txt (mesmo usado pelo V2 migration)
    // simula o estado inicial do banco com 20 registros legados
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
