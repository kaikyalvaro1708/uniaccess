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

    public String generate(String fullName, LoginAvailabilityChecker checker) {
        var tokens = normalize(fullName);

        for (var candidate : buildCandidates(tokens)) {
            if (checker.isAvailable(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not generate a unique login for: " + fullName);
    }

    String[] normalize(String fullName) {
        var decomposed = Normalizer.normalize(fullName, Normalizer.Form.NFD);
        var clean = decomposed
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\\s]", "")
                .trim();

        return Arrays.stream(clean.split("\\s+"))
                .filter(t -> !t.isEmpty())
                .toArray(String[]::new);
    }

    List<String> buildCandidates(String[] tokens) {
        if (tokens.length == 0) throw new IllegalArgumentException("Name has no usable letters");

        var candidates = new ArrayList<String>();
        var seen = new HashSet<String>();

        var first = tokens[0];
        var base = first.length() >= LENGTH ? first.substring(0, LENGTH) : first;

        // first name + greedy fill from next tokens
        addCandidate(greedyFill(base, tokens, 1), candidates, seen);

        // vary how many chars are taken from each subsequent token
        for (int t = 1; t < tokens.length; t++) {
            var tok = tokens[t];
            for (int n = 1; n <= tok.length(); n++) {
                var sb = new StringBuilder(base);
                sb.append(tok, 0, n);
                greedyAppend(sb, tokens, t + 1);
                pad(sb);
                addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
            }
        }

        // one initial from each subsequent token
        if (tokens.length >= 2) {
            var sb = new StringBuilder(base);
            for (int t = 1; t < tokens.length && sb.length() < LENGTH; t++) {
                sb.append(tokens[t].charAt(0));
            }
            pad(sb);
            addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
        }

        // slide start offset inside each subsequent token
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

        // invert: first name + last surnames first
        for (int t = tokens.length - 1; t >= 1; t--) {
            var sb = new StringBuilder(base);
            var tok = tokens[t];
            for (int i = 0; i < tok.length() && sb.length() < LENGTH; i++) {
                sb.append(tok.charAt(i));
            }
            pad(sb);
            addCandidate(sb.toString().substring(0, LENGTH), candidates, seen);
        }

        // surname-first
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

        // fallback: sliding window over the cycled name pool
        var pool = buildPool(tokens);
        for (int i = 0; i + LENGTH <= pool.length(); i++) {
            addCandidate(pool.substring(i, i + LENGTH), candidates, seen);
        }

        // deep fallback: enumerate combinations from unique letter pool
        int[] count = {0};
        enumerateCombinations(uniqueLetters(tokens), new char[LENGTH], 0, candidates, seen, count);

        return candidates;
    }

    private String greedyFill(String base, String[] tokens, int from) {
        var sb = new StringBuilder(base);
        greedyAppend(sb, tokens, from);
        pad(sb);
        return sb.toString().substring(0, LENGTH);
    }

    private void greedyAppend(StringBuilder sb, String[] tokens, int from) {
        for (int t = from; t < tokens.length && sb.length() < LENGTH; t++) {
            for (int i = 0; i < tokens[t].length() && sb.length() < LENGTH; i++) {
                sb.append(tokens[t].charAt(i));
            }
        }
    }

    private void pad(StringBuilder sb) {
        while (sb.length() < LENGTH) sb.append('a');
    }

    private String buildPool(String[] tokens) {
        var raw = String.join("", tokens);
        while (raw.length() < LENGTH * 2) raw += raw;
        return raw;
    }

    private char[] uniqueLetters(String[] tokens) {
        var present = new boolean[26];
        int count = 0;
        for (var tok : tokens) {
            for (char c : tok.toCharArray()) {
                int idx = c - 'a';
                if (!present[idx]) { present[idx] = true; count++; }
            }
        }
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

    private void enumerateCombinations(char[] alphabet, char[] current, int depth,
                                        List<String> candidates, Set<String> seen, int[] count) {
        if (count[0] >= 8000) return;
        if (depth == LENGTH) {
            addCandidate(new String(current), candidates, seen);
            count[0]++;
            return;
        }
        for (char c : alphabet) {
            current[depth] = c;
            enumerateCombinations(alphabet, current, depth + 1, candidates, seen, count);
            if (count[0] >= 8000) return;
        }
    }

    private void addCandidate(String candidate, List<String> candidates, Set<String> seen) {
        if (candidate.length() == LENGTH && candidate.matches("[a-z]{7}") && seen.add(candidate)) {
            candidates.add(candidate);
        }
    }
}
