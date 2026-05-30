package com.itau.identityprovisioning.login;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LoginGeneratorDemo {

    public static void main(String[] args) throws Exception {
        var existing = loadExistingLogins();
        var generator = new LoginGenerator();
        LoginAvailabilityChecker checker = login -> !existing.contains(login);

        System.out.println("--------------------------------------------");
        System.out.println("  GERADOR DE LOGIN - demo");
        System.out.println("--------------------------------------------");
        System.out.printf("  %d logins existentes carregados de massa_dados.txt%n%n", existing.size());

        // names that collide with seed data
        String[] collisionNames = {
            "Maria Silva Santos",
            "Maria Simoes Andrade",
            "Joao Pedro Alves",
            "Ana Clara Souza",
            "Carlos Eduardo Lima"
        };

        System.out.println("  [tratamento de colisao]");
        for (var name : collisionNames) {
            var login = generator.generate(name, checker);
            System.out.printf("  %-30s -> %s%n", name, login);
        }

        System.out.println();

        // fresh names — no collision, shows first deterministic candidate
        String[] freshNames = {
            "Fernanda Souza Lima",
            "Jose Alvaro Conceicao",
            "Lucas Henrique Costa",
            "Ana Bo"
        };

        System.out.println("  [primeiro candidato - sem colisao]");
        for (var name : freshNames) {
            var login = generator.generate(name, l -> true);
            System.out.printf("  %-30s -> %s%n", name, login);
        }

        System.out.println();

        // accent and cedilha normalisation
        String[] accentNames = {
            "José Álvaro Conceição",
            "François Dupont",
            "Renânia Ângelo"
        };

        System.out.println("  [normalizacao de acentos / cedilha]");
        for (var name : accentNames) {
            var login = generator.generate(name, l -> true);
            System.out.printf("  %-30s -> %s%n", name, login);
        }

        System.out.println();

        // invariant check
        System.out.println("  [invariantes: exatamente 7 chars, somente a-z, sem digitos]");
        String[] allNames = {
            "Maria Silva Santos",
            "José Álvaro Pereira",
            "Ana Bo",
            "Carlos Eduardo Lima",
            "Lia"
        };

        var allOk = true;
        for (var name : allNames) {
            var login = generator.generate(name, l -> true);
            var ok = login.length() == 7 && login.matches("[a-z]{7}");
            System.out.printf("  %-30s -> %-10s length=%-2d a-z=%s%n",
                    name, login, login.length(), ok ? "OK" : "FAIL");
            if (!ok) allOk = false;
        }

        System.out.println();
        System.out.println(allOk
                ? "  Todos os invariantes passaram."
                : "  FALHOU: um ou mais invariantes violados.");
        System.out.println("--------------------------------------------");
    }

    private static Set<String> loadExistingLogins() throws Exception {
        var logins = new HashSet<String>();
        InputStream is = LoginGeneratorDemo.class.getClassLoader()
                .getResourceAsStream("massa_dados.txt");

        if (is == null) return logins;

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
