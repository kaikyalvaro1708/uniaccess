package com.itau.identityprovisioning.login;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


@Component
public class LoginGenerator {

    private static final int LENGTH = 7;

    /**
     * Ponto de entrada público.
     * @param fullName  nome completo da pessoa (pode ter acentos)
     * @param checker   lambda que consulta o banco: retorna true se o login está livre
     */
    public String generate(String fullName, LoginAvailabilityChecker checker) {
        // normaliza o nome em tokens limpos ["joao", "silva", "santos"]
        var tokens = normalize(fullName);

        // gera lista ordenada de candidatos
        for (var candidate : buildCandidates(tokens)) {
            // retorna o primeiro candidato que ainda não existe no banco
            if (checker.isAvailable(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not generate a unique login for: " + fullName);
    }

    // ─── normalização ─────────────────────────────────────────────────────────
    //Converte o nome completo em array de tokens ASCII minúsculos sem acento.
    //Exemplo: "João Pedro-Silva" → ["joao", "pedrosilva"]
    String[] normalize(String fullName) {
        // NFD decompõe cada letra acentuada em letra-base + marca separada
        // ex: "ã" → "a" + til (caractere invisível)
        var decomposed = Normalizer.normalize(fullName, Normalizer.Form.NFD);

        var clean = decomposed
                // remove todas as marcas diacríticas (acentos, tils, cedilhas)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                // converte para minúsculas
                .toLowerCase(Locale.ROOT)
                // remove números, hífens, símbolos
                .replaceAll("[^a-z\\s]", "")
                .trim();

        // divide por um ou mais espaços e descarta tokens vazios
        return Arrays.stream(clean.split("\\s+"))
                .filter(t -> !t.isEmpty())
                .toArray(String[]::new);
    }

    // ─── construção de candidatos ─────────────────────────────────────────────
    //Gera todos os candidatos de login em ordem de prioridade.
    List<String> buildCandidates(String[] tokens) {
        if (tokens.length == 0) throw new IllegalArgumentException("Name has no usable letters");

        var candidates = new ArrayList<String>();
        var seen = new HashSet<String>(); // controle de duplicatas dentro da lista

        var first = tokens[0]; // primeiro nome: "joao"

        // base = primeiros 7 chars do primeiro nome, ou o nome inteiro se menor que 7
        // ex: "joao" (4 chars) → base = "joao" | "fernando" (8 chars) → base = "fernand"
        var base = first.length() >= LENGTH ? first.substring(0, LENGTH) : first;

        // ── estratégia 1: primeiro nome + preenchimento greedy dos sobrenomes ──
        // ex: base="joao" + "ped" de "pedro" = "joaoped"
        addCandidate(greedyFill(base, tokens, 1), candidates, seen);

        // ── estratégia 2: varia quantas letras pega de cada sobrenome ────────
        // ex: base="joao" + "p" = "joaop..." | "pe" = "joaope..." | "ped" = "joaoped"
        for (int t = 1; t < tokens.length; t++) {
            var tok = tokens[t];
            for (int n = 1; n <= tok.length(); n++) {
                var sb = new StringBuilder(base);
                sb.append(tok, 0, n);           // adiciona n letras do sobrenome atual
                greedyAppend(sb, tokens, t + 1); // preenche o resto com os próximos tokens
                pad(sb);                         // completa com 'a' se ainda faltam chars
                addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
            }
        }

        // ── estratégia 3: uma inicial de cada sobrenome ──────────────────────
        // ex: base="joao" + "p" + "a" (de Alves) = "joaopa?"
        if (tokens.length >= 2) {
            var sb = new StringBuilder(base);
            for (int t = 1; t < tokens.length && sb.length() < LENGTH; t++) {
                sb.append(tokens[t].charAt(0)); // apenas a primeira letra de cada token
            }
            pad(sb);
            addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
        }

        // ── estratégia 4: offset dentro do sobrenome (pula letras do início) ─
        // ex: em vez de "silva" → usa "ilva", "lva", "va"...
        for (int t = 1; t < tokens.length; t++) {
            var tok = tokens[t];
            for (int offset = 1; offset < tok.length(); offset++) {
                var sb = new StringBuilder(base);
                for (int i = offset; i < tok.length() && sb.length() < LENGTH; i++) {
                    sb.append(tok.charAt(i));
                }
                greedyAppend(sb, tokens, t + 1);
                pad(sb);
                addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
            }
        }

        // ── estratégia 5: inverte a prioridade — usa o último sobrenome primeiro ─
        // ex: base="joao" + letras de "Alves" antes de "Pedro"
        for (int t = tokens.length - 1; t >= 1; t--) {
            var sb = new StringBuilder(base);
            var tok = tokens[t];
            for (int i = 0; i < tok.length() && sb.length() < LENGTH; i++) {
                sb.append(tok.charAt(i));
            }
            pad(sb);
            addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
        }

        // ── estratégia 6: sobrenome como base + letras do primeiro nome ──────
        // ex: "silva" (5) + "jo" (de joao) = "silvajo"
        for (int t = tokens.length - 1; t >= 1; t--) {
            var tok = tokens[t];
            var surnameBase = tok.length() >= LENGTH ? tok.substring(0, LENGTH) : tok;
            var sb = new StringBuilder(surnameBase);
            for (int i = 0; i < first.length() && sb.length() < LENGTH; i++) {
                sb.append(first.charAt(i));
            }
            pad(sb);
            addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
        }

        // ── estratégia 7: janela deslizante sobre todas as letras concatenadas ─
        // ex: "joaosilva" → "joaosil", "oaosila", "aosilav"...
        var pool = buildPool(tokens);
        for (int i = 0; i + LENGTH <= pool.length(); i++) {
            addCandidate(pool.substring(i, i + LENGTH), candidates, seen);
        }

        // ── estratégia 8: fallback — combinações das letras únicas do nome ───
        // só chega aqui se todos os candidatos anteriores estiverem ocupados no banco
        // gera até 8.000 combinações para garantir que sempre encontra um login disponível
        int[] count = {0};
        enumerateCombinations(uniqueLetters(tokens), new char[LENGTH], 0, candidates, seen, count);

        return candidates;
    }

    // ─── métodos auxiliares ───────────────────────────────────────────────────

    // preenche o StringBuilder com letras dos próximos tokens até atingir LENGTH
    private String greedyFill(String base, String[] tokens, int from) {
        var sb = new StringBuilder(base);
        greedyAppend(sb, tokens, from);
        pad(sb);
        return sb.toString().substring(0, LENGTH);
    }

    // appenda letras dos tokens a partir de 'from' até o sb ter LENGTH caracteres
    private void greedyAppend(StringBuilder sb, String[] tokens, int from) {
        for (int t = from; t < tokens.length && sb.length() < LENGTH; t++) {
            for (int i = 0; i < tokens[t].length() && sb.length() < LENGTH; i++) {
                sb.append(tokens[t].charAt(i));
            }
        }
    }

    // completa o StringBuilder com 'a' até atingir LENGTH (para nomes muito curtos)
    private void pad(StringBuilder sb) {
        while (sb.length() < LENGTH) sb.append('a');
    }

    // concatena todos os tokens e duplica até ter pelo menos LENGTH*2 chars
    // garante que a janela deslizante (estratégia 7) tenha material suficiente
    private String buildPool(String[] tokens) {
        var raw = String.join("", tokens);
        while (raw.length() < LENGTH * 2) raw += raw;
        return raw;
    }

    // retorna as letras únicas do nome (ou o alfabeto completo se houver poucas)
    private char[] uniqueLetters(String[] tokens) {
        var present = new boolean[26];
        int count = 0;
        for (var tok : tokens) {
            for (char c : tok.toCharArray()) {
                int idx = c - 'a'; // 'a'=0, 'b'=1, ..., 'z'=25
                if (!present[idx]) { present[idx] = true; count++; }
            }
        }
        // se há letras suficientes, usa só as do nome; senão usa o alfabeto inteiro
        if (count >= LENGTH) {
            var letters = new char[count];
            int j = 0;
            for (int i = 0; i < 26; i++) {
                if (present[i]) letters[j++] = (char) ('a' + i);
            }
            return letters;
        }
        var letters = new char[26];
        for (int i = 0; i < 26; i++) letters[i] = (char) ('a' + i);
        return letters;
    }

    // recursão que enumera combinações do alfabeto até gerar 8000 candidatos
    private void enumerateCombinations(char[] alphabet, char[] current, int depth,
                                        List<String> candidates, Set<String> seen, int[] count) {
        if (count[0] >= 8000) return; // limite de segurança para não travar
        if (depth == LENGTH) {
            addCandidate(new String(current), candidates, seen);
            count[0]++;
            return;
        }
        for (char c : alphabet) {
            current[depth] = c; // coloca a letra na posição atual
            enumerateCombinations(alphabet, current, depth + 1, candidates, seen, count);
            if (count[0] >= 8000) return;
        }
    }

    // adiciona o candidato à lista apenas se:
    // 1. tem exatamente 7 chars  2. só contém [a-z]  3. ainda não foi adicionado
    private void addCandidate(String candidate, List<String> candidates, Set<String> seen) {
        if (candidate.length() == LENGTH && candidate.matches("[a-z]{7}") && seen.add(candidate)) {
            candidates.add(candidate);
        }
    }
}
