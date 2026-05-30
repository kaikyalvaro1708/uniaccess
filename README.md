<div align="center">

<br/>

# Identity Provisioning API

**Desafio Técnico — Engenharia de Software Jr.**

<br/>

![Java](https://img.shields.io/badge/Java-21-white?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=003B71)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-white?style=for-the-badge&logo=springboot&logoColor=white&labelColor=003B71)
![Maven](https://img.shields.io/badge/Maven-Build-white?style=for-the-badge&logo=apachemaven&logoColor=white&labelColor=003B71)
![JUnit 5](https://img.shields.io/badge/JUnit-5-white?style=for-the-badge&logo=junit5&logoColor=white&labelColor=003B71)

</div>

---

## Sobre o desafio

O case pede a criação de uma aplicação de **cadastro de pessoas** com geração automática de login. O sistema deve:

- Receber os dados da pessoa (nome, documento, e-mail, data de nascimento, CEP)
- Validar todos os campos
- Consultar o endereço automaticamente via CEP (ViaCEP)
- Persistir os dados
- **Gerar automaticamente um login único** a partir do nome da pessoa
- Retornar os dados cadastrados com o login gerado

---

## O que é a geração automática de login?

Quando uma pessoa é cadastrada no sistema, ela recebe automaticamente um **login único** derivado do seu nome — sem precisar escolher um. Isso elimina fricção no cadastro e garante um padrão consistente de identificação.

Exemplos diretos:
| Nome | Login gerado |
|---|---|
| Maria Silva Santos | `mariasi` |
| José Álvaro Conceição | `josealv` |
| João Pedro Lima | `joaoped` |
| Ana Bo | `anaboaa` |

---

## Regras do login — o que foi pedido vs o que foi feito

### O que o case exige

| # | Regra exigida |
|---|---|
| 1 | Exatamente **7 caracteres** |
| 2 | **Único** — não pode se repetir para duas pessoas |
| 3 | **Sem espaços** |
| 4 | **Sem números** |
| 5 | Construído a partir de **informações do nome da pessoa** |
| 6 | Apenas **letras minúsculas (a-z)** |
| 7 | Em caso de colisão, gerar uma **variação sem quebrar as regras** (sem usar números) |

### O que foi implementado e por quê

**Regra 1 — Exatamente 7 caracteres**  
O gerador sempre produz candidatos com exatamente 7 caracteres. Se o nome tiver menos de 7 letras úteis, o login é completado com `'a'` (ex.: `"Lia"` → `"liaaaaa"`). O número 7 é verificado tanto na geração quanto nos testes automatizados.

**Regra 2 — Unicidade**  
A verificação de disponibilidade é injetada como interface funcional (`LoginAvailabilityChecker`). O gerador produz uma lista ordenada de candidatos e retorna o **primeiro disponível** de acordo com o checker fornecido. Isso desacopla o algoritmo da persistência e permite testar unicidade em memória.

**Regra 3 — Sem espaços**  
O nome é dividido em tokens no pré-processamento. Os candidatos são construídos concatenando partes dos tokens — nunca há espaços no resultado.

**Regra 4 — Sem números**  
A normalização descarta qualquer caractere que não seja `[a-z]` com uma regex. Dígitos no nome de entrada também são removidos antes de qualquer combinação.

**Regra 5 — Derivado do nome**  
Todos os candidatos são construídos exclusivamente a partir das letras do nome normalizado. Nenhuma letra é inventada — o padding usa `'a'`, que é a letra mais neutra do alfabeto e é retirada do pool mínimo possível.

**Regra 6 — Letras minúsculas a-z**  
A normalização converte o nome para lowercase e remove diacríticos via decomposição NFD (`José` → `jose`, `François` → `francois`). O resultado contém apenas `[a-z]`.

**Regra 7 — Colisão sem números**  
Como o case proíbe números, a estratégia de desambiguação usa **variações da própria combinação de letras do nome**, em seis camadas progressivas (detalhadas abaixo). Isso garante que sempre existe um login disponível sem nunca precisar de sufixos numéricos.

---

## Gerador de Login — como o algoritmo funciona

O `LoginGenerator` segue uma sequência de etapas. Em cada etapa gera candidatos e verifica disponibilidade. O **primeiro candidato disponível** é retornado.

### Passo 1 — Normalização

Antes de gerar qualquer candidato, o nome é limpo:

```
"José Álvaro Conceição"
  → decomposição NFD + remoção de diacríticos → "Jose Alvaro Conceicao"
  → lowercase                                  → "jose alvaro conceicao"
  → remove tudo que não é [a-z]               → "jose alvaro conceicao"
  → divide em tokens                           → ["jose", "alvaro", "conceicao"]
```

### Passo 2 — Primeiro nome + sobrenome (principal)

Começa pelo primeiro nome e completa com letras dos sobrenomes:

```
["maria", "silva", "santos"]
  → maria + si  → "mariasi"   ← 1º candidato
  → maria + ss  → "mariass"   ← 2º candidato
  → maria + sa  → "mariasa"   ← 3º candidato
  ...
```

### Passo 3 — Iniciais dos sobrenomes

```
["maria", "silva", "santos"]  →  maria + s + s  →  "mariass"
```

### Passo 4 — Deslocamento dentro dos tokens

Varia o ponto de início dentro de cada sobrenome:

```
["joao", "pedro", "alves"]
  → joao + edro + ... → "joaoedr"
  → joao + dro  + ... → "joaodro"
```

### Passo 5 — Inversão de prioridade

Usa o último sobrenome como base principal:

```
["maria", "silva", "santos"]
  → santos + maria → "santoam"
  → silva  + maria → "silvama"
```

### Passo 6 — Fallback: janela deslizante

Concatena todas as letras do nome e desliza uma janela de 7 posições:

```
"mariasilvasan tos" → "mariasi", "ariasil", "riasilvs", ...
```

### Passo 7 — Fallback profundo

Enumera sistematicamente combinações do pool de letras únicas do nome (até 8.000 combinações). Garante que sempre existe um login disponível, mesmo em cenários extremos.

### Padding

Se o nome tiver menos de 7 letras úteis, o login é completado com `'a'`:

```
"Ana Bo"  → ["ana", "bo"]  → anabo + aa  → "anaboaa"
"Lia"     → ["lia"]        → lia   + aaaa → "liaaaaa"
```

---

## Injeção de dependência

A verificação de disponibilidade é injetada como interface funcional. O algoritmo não conhece banco de dados — ele só pergunta "esse login está disponível?":

```java
// em produção: consulta o banco de dados
LoginAvailabilityChecker checker = login -> !repository.existsByLogin(login);

// em testes: conjunto em memória
LoginAvailabilityChecker checker = login -> !existingLogins.contains(login);

String login = generator.generate("Maria Silva Santos", checker);
// → "mariasi"
```

---

## Rodando o demo

Compile e execute o `LoginGeneratorDemo` para ver o gerador em ação:

```bash
# a partir do diretório backend/identity-provisioning-api
JAVA="/c/Program Files/Java/jdk-21/bin/java"
JAVAC="/c/Program Files/Java/jdk-21/bin/javac"

$JAVAC -cp target/classes \
  src/main/java/com/itau/identityprovisioning/login/LoginAvailabilityChecker.java \
  src/main/java/com/itau/identityprovisioning/login/LoginGenerator.java \
  src/main/java/com/itau/identityprovisioning/login/LoginGeneratorDemo.java \
  -d target/classes

$JAVA -cp "target/classes:src/main/resources" \
  com.itau.identityprovisioning.login.LoginGeneratorDemo
```

Saída esperada:

```
--------------------------------------------
  GERADOR DE LOGIN - demo
--------------------------------------------
  20 logins existentes carregados de massa_dados.txt

  [tratamento de colisao]
  Maria Silva Santos             -> mariail
  Maria Simoes Andrade           -> mariaaa
  Joao Pedro Alves               -> joaopal
  Ana Clara Souza                -> anacsou
  Carlos Eduardo Lima            -> carlosl

  [primeiro candidato - sem colisao]
  Fernanda Souza Lima            -> fernand
  Jose Alvaro Conceicao          -> josealv
  Lucas Henrique Costa           -> lucashe
  Ana Bo                         -> anaboaa

  [normalizacao de acentos / cedilha]
  José Álvaro Conceição          -> josealv
  François Dupont                -> francoi
  Renânia Ângelo                 -> renania

  [invariantes: exatamente 7 chars, somente a-z, sem digitos]
  Maria Silva Santos             -> mariasi    length=7  a-z=OK
  José Álvaro Pereira            -> josealv    length=7  a-z=OK
  Ana Bo                         -> anaboaa    length=7  a-z=OK
  Carlos Eduardo Lima            -> carlose    length=7  a-z=OK
  Lia                            -> liaaaaa    length=7  a-z=OK

  Todos os invariantes passaram.
--------------------------------------------
```

---

## Rodando os testes

```bash
JAVA_HOME="/c/Program Files/Java/jdk-21" mvn test -Dtest=LoginGeneratorTest
```

A suite de testes cobre:

- **Caminho feliz** — primeiro candidato determinístico para nomes conhecidos (`mariasi`, `joaoped`, `anaclar`, `carlose`)
- **Colisão** — dados de `massa_dados.txt` usados como logins existentes; o gerador deve encontrar uma alternativa livre
- **Normalização** — vogais acentuadas, cedilha, diacríticos mistos, dígitos na entrada
- **Nomes curtos** — menos de 7 letras úteis ainda produzem um login válido de 7 chars
- **Invariantes** — `length == 7`, `matches("[a-z]{7}")`, sem dígitos, sem maiúsculas, sem espaços

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Build | Maven |
| Testes | JUnit 5 |

---

## Estrutura do projeto

```
backend/identity-provisioning-api/
└── src/
    ├── main/
    │   ├── java/com/itau/identityprovisioning/
    │   │   └── login/
    │   │       ├── LoginAvailabilityChecker.java   # porta (interface funcional)
    │   │       ├── LoginGenerator.java              # algoritmo
    │   │       └── LoginGeneratorDemo.java          # demo executável
    │   └── resources/
    │       └── massa_dados.txt                      # dados de semente (20 logins existentes)
    └── test/
        └── java/com/itau/identityprovisioning/
            └── login/
                └── LoginGeneratorTest.java          # suite JUnit 5
```

---

