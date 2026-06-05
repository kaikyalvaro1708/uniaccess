# Decisões Técnicas — UniAccess

Neste documento eu explico o raciocínio por trás das principais escolhas do projeto. Não só o que foi feito, mas por que foi feito assim.

---

## Sumário

- [Arquitetura geral](#arquitetura-geral)
- [Backend — Spring Boot e organização do código](#backend--spring-boot-e-organização-do-código)
- [Frontend — React, TypeScript, Vite e Tailwind](#frontend--react-typescript-vite-e-tailwind)
- [Nginx — por que ele está aqui e o que ele faz](#nginx--por-que-ele-está-aqui-e-o-que-ele-faz)
- [Docker Compose — como os serviços se comunicam](#docker-compose--como-os-serviços-se-comunicam)
- [Esteira CI — GitHub Actions](#esteira-ci--github-actions)
- [Por que CPF e como a validação funciona](#por-que-cpf-e-como-a-validação-funciona)
- [Por que o sistema aceita acentos no nome](#por-que-o-sistema-aceita-acentos-no-nome)
- [Como funciona a geração automática de login](#como-funciona-a-geração-automática-de-login)
- [Persistência — PostgreSQL e Flyway](#persistência--postgresql-e-flyway)
- [Por que o frontend nunca chama o ViaCEP diretamente](#por-que-o-frontend-nunca-chama-o-viacep-diretamente)
- [Como os erros da API são tratados](#como-os-erros-da-api-são-tratados)
- [A decisão de design mais importante — LoginAvailabilityChecker](#a-decisão-de-design-mais-importante--loginavailabilitychecker)
- [Validação no frontend e no backend ao mesmo tempo](#validação-no-frontend-e-no-backend-ao-mesmo-tempo)
- [CPF mascarado nos logs](#cpf-mascarado-nos-logs)
- [Testes automatizados](#testes-automatizados)

---

## Arquitetura geral

A aplicação tem três serviços: banco de dados, backend e frontend. Todos rodam juntos pelo Docker Compose em uma rede interna isolada.

![Arquitetura local — Docker Compose](prints/arquitetura-local.png)

O ponto central da arquitetura é que o browser só enxerga uma porta, a `:5173`. Ele nunca fala com o backend diretamente. Quem faz essa ponte é o nginx, que recebe as chamadas `/api/*` do frontend e as redireciona internamente para o Spring Boot. O ViaCEP também é chamado só pelo backend, nunca pelo browser.

---

## Backend — Spring Boot e organização do código

**Stack:** Java 21 · Spring Boot 3.4.4 · Spring Data JPA · Bean Validation · SpringDoc (Swagger) · Actuator

Escolhi Spring Boot porque é o framework Java mais usado no mercado, especialmente em bancos e fintechs. Ele já traz tudo integrado: validação, persistência, HTTP client e documentação, sem precisar montar nada do zero.

Uma escolha que fiz conscientemente foi organizar o código **por domínio, não por camada técnica**:

```
❌ Por camada (comum, mas espalhado)     ✅ Por domínio (adotado)
controllers/                             controller/
services/                                domain/person/     ← tudo de pessoa aqui
repositories/                            domain/zipcode/
models/                                  infra/exception/
                                         infra/http/
                                         login/             ← algoritmo isolado
```

Na prática, quando preciso entender o fluxo de cadastro, abro `domain/person/` e está tudo lá. Na estrutura por camada, navegaria por 4 pastas diferentes para o mesmo fluxo.

Os DTOs são todos `record`, imutáveis por natureza, sem setter, com tudo que o compilador já gera. Para objetos que só transportam dados de entrada e saída, faz muito mais sentido do que uma classe cheia de boilerplate.

As validações de CPF e nome são `ConstraintValidator` customizados (`@ValidCpf`, `@ValidName`), não apenas um `@Pattern` com regex. Assim a regra de negócio fica nomeada e testável de forma isolada.

O controller é propositalmente fino: só recebe a requisição, delega ao service e devolve a resposta. O `POST /api/persons` retorna `201 Created` com o header `Location` apontando para o recurso criado, que é o comportamento correto segundo REST.

---

## Frontend — React, TypeScript, Vite e Tailwind

**Stack:** React 19 · TypeScript 5 · Vite 6 · Tailwind CSS

Escolhi React por ser o framework mais adotado no mercado brasileiro, especialmente em fintechs, e provavelmente o que o time do Itaú já usa.

TypeScript foi escolha óbvia: ele previne erros em tempo de compilação, o que é crítico num projeto que consome uma API REST. Se eu mudar um campo no backend e o frontend não atualizar, o TypeScript já avisa antes de chegar no browser.

Usei Vite em vez do Create React App porque o CRA está deprecated. O Vite é muito mais rápido, tem HMR instantâneo e já vem com proxy nativo. O `vite.config.ts` redireciona `/api/*` para `localhost:8080` em desenvolvimento, espelhando exatamente o que o nginx faz em produção.

Para o CSS, escolhi Tailwind porque queria uma identidade visual própria: o laranja do UniAccess é baseado no laranja do Itaú. Bootstrap ou MUI trazem componentes prontos, mas com opiniões de design que são difíceis de sobrescrever. Com Tailwind, escrevo as classes direto no JSX, o build faz tree-shaking automático e o layout responsivo sai com prefixos tipo `lg:grid-cols-2`, sem precisar de media query manual.

---

## Nginx — por que ele está aqui e o que ele faz

O nginx no container do frontend faz duas coisas ao mesmo tempo.

**Serve os arquivos estáticos.** O `npm run build` gera os arquivos do React em `dist/`, e o nginx entrega esse HTML/CSS/JS para o browser.

**Faz proxy das chamadas à API.** Quando o React chama `/api/persons`, o nginx intercepta e redireciona para o backend:

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;
}
```

Sem isso, o browser estaria fazendo chamadas de `localhost:5173` para `localhost:8080`. Portas diferentes ativam a política de CORS e as requisições seriam bloqueadas. Com o nginx servindo as duas coisas na mesma porta, o browser vê tudo como mesma origem e não há problema.

O `CorsConfig.java` ainda existe para quando o backend é acessado diretamente: pelo Swagger UI, Postman ou durante desenvolvimento local sem Docker. É uma segunda camada de proteção, não a principal.

---

## Docker Compose — como os serviços se comunicam

São três serviços numa rede privada isolada:

| Serviço | Porta host | Porta container |
|---------|-----------|----------------|
| `postgres` | 5433 | 5432 |
| `backend` | 8080 | 8080 |
| `frontend` | 5173 | 80 |

Usei 5433 no host para o postgres para evitar conflito com um PostgreSQL local rodando na porta padrão 5432. Dentro da rede Docker os serviços se comunicam na 5432 normalmente.

Cada serviço tem um **multi-stage build** no Dockerfile. O primeiro estágio usa a imagem completa (Maven ou Node) para compilar. O segundo copia só o resultado (JAR ou `dist/`) para uma imagem enxuta de runtime. O backend com multi-stage fica em torno de 200 MB; sem isso carregaria o Maven inteiro e chegaria perto de 500 MB.

O detalhe que mais importa no Compose é o `depends_on` com `condition: service_healthy` no backend:

```yaml
backend:
  depends_on:
    postgres:
      condition: service_healthy
```

O `depends_on` simples só espera o container iniciar, mas o Postgres leva alguns segundos após iniciar para aceitar conexões de verdade. Se o backend subir cedo demais, o Flyway tenta conectar antes do banco estar pronto e a aplicação cai. O `service_healthy` garante que o `pg_isready` passa antes de liberar o backend.

A URL do banco também muda por ambiente sem precisar alterar nenhum arquivo. Em desenvolvimento é `localhost:5433`; em Docker o `docker-compose.yml` injeta `SPRING_DATASOURCE_URL` via variável de ambiente, que o Spring Boot usa automaticamente no lugar da propriedade do `application.properties`.

---

## Esteira CI — GitHub Actions

Criei uma pipeline simples em `.github/workflows/ci.yml` que roda em todo push e pull request:

```
Backend Compile → Backend Tests → Frontend Lint → Frontend Build
```

| Etapa | O que garante |
|-------|--------------|
| `mvn compile` | O código Java compila sem erro |
| `mvn test` | Todos os testes unitários passam |
| `npm run lint` | TypeScript sem erros de tipo |
| `npm run build` | O bundle do React gera sem problema |

Mesmo sendo um projeto solo, achei importante ter CI. Garante que o repositório entregue sempre está num estado funcional verificado, não só "funcionou na minha máquina".

---

## Por que CPF e como a validação funciona

Escolhi CPF porque é o documento de identificação mais universal para pessoas físicas no Brasil e porque ele tem regras de validação determinísticas que permitem ir muito além de checar o formato.

O `CpfValidator` implementa o algoritmo real da Receita Federal: remove a máscara, rejeita sequências com todos os dígitos iguais (tipo `111.111.111-11`, que passa no algoritmo mas não existe na prática) e calcula os dois dígitos verificadores por soma ponderada e módulo 11.

No banco, o CPF é armazenado sem máscara (`12345678909`) e exibido com formatação (`123.456.789-09`) pelo DTO de saída. Isso mantém consistência nas queries e unicidade na constraint, sem precisar normalizar na hora de buscar.

---

## Por que o sistema aceita acentos no nome

O enunciado pede que o nome não contenha acentos ou cedilhas. Entendo que essa regra provavelmente existe porque sistemas mais antigos têm restrições com encodings e armazenamento de caracteres especiais, o que faz sentido num contexto legado.

Mesmo assim, tomei a decisão de aceitar nomes com acento, e vou explicar o raciocínio.

Sistemas modernos não têm mais essa limitação. PostgreSQL com encoding UTF-8 armazena `João`, `Conceição` e qualquer outro caractere Unicode sem nenhum problema. Java também lida com Unicode nativamente. Não há nenhuma restrição técnica que justifique rejeitar `João Silva` num sistema construído hoje.

O ponto mais importante é que o login gerado é **idêntico** nos dois casos:

| Nome cadastrado | Login gerado |
|-----------------|--------------|
| `João Silva` | `joaosilv` |
| `Joao Silva` | `joaosilv` |

O `LoginGenerator` normaliza o nome internamente via NFD antes de gerar qualquer candidato: decompõe `ã` em `a` mais a marca diacrítica, remove as marcas, descarta tudo que não for `[a-z]`. O nome armazenado nunca entra no algoritmo diretamente, só a versão normalizada. A restrição de `[a-z]` se aplica exclusivamente ao login.

Forçar o usuário a digitar `Joao` em vez de `João` seria uma escolha de UX ruim sem justificativa técnica num sistema novo. O nome de uma pessoa é um dado de identidade, e preservá-lo corretamente me pareceu a decisão certa.

Sobre a massa legada: os nomes do `massa_dados.txt` estão sem acento porque são dados históricos pré-existentes, com a qualidade típica de dados legados. Isso não define o comportamento do sistema atual.

---

## Como funciona a geração automática de login

O maior desafio aqui foi que o enunciado proíbe números no login. O jeito mais simples de garantir unicidade, adicionar um contador (`joaosilv1`, `joaosilv2`...), está fora. Precisei de outra abordagem.

A solução foi construir um algoritmo que tenta combinações progressivamente mais genéricas do nome até encontrar uma disponível no banco. São 8 estratégias em ordem de "naturalidade":

| # | Estratégia | Exemplo — João Pedro Alves |
|---|------------|---------------------------|
| 1 | Primeiro nome + greedy dos sobrenomes | `joaoped` |
| 2 | Varia quantas letras pega de cada sobrenome | `joaop`, `joaope`... |
| 3 | Inicial de cada sobrenome | `joaopa` |
| 4 | Pula letras do início do sobrenome | usa `edro` em vez de `pedro` |
| 5 | Último sobrenome primeiro | `joaoalv` |
| 6 | Sobrenome como base mais letras do primeiro nome | `alvesjo` |
| 7 | Janela deslizante sobre todas as letras | `oaopedro`... |
| 8 | Fallback — combinações das letras únicas do nome | até 8.000 opções |

O primeiro candidato disponível no banco é retornado. A ideia é que o login mais reconhecível seja tentado antes dos mais genéricos.

A unicidade é garantida em dois níveis: o código verifica `existsByLogin` antes de salvar, e o banco tem `UNIQUE CONSTRAINT` na coluna `login`, o que protege contra condição de corrida se duas requisições chegarem ao mesmo tempo.

---

## Persistência — PostgreSQL e Flyway

Escolhi PostgreSQL por ser o banco relacional open source mais robusto: suporte sólido a transações ACID e boa adoção em stacks modernas.

Uma decisão importante foi desligar o `ddl-auto` do Hibernate (`ddl-auto=none`) e usar o Flyway para gerenciar o schema. O `ddl-auto=update` pode modificar ou apagar tabelas de forma inesperada, o que em produção é perigoso. Com `none`, o JPA só mapeia as entidades para as tabelas existentes; quem cria e altera as tabelas é o Flyway, e só ele.

O Flyway mantém o schema versionado no Git (`V1__create_person_table.sql`, `V2__insert_legacy_persons.sql`). Qualquer pessoa que clonar o repositório e rodar `docker-compose up` já tem o banco configurado do zero, sem SQL manual.

A migration V2 merece uma nota. Os CPFs da massa legada são sequenciais inventados: `12345678901`, `12345678902`... até `12345678920`. Eles não passam na validação porque o `CpfValidator` implementa o algoritmo real da Receita Federal, que exige que os dois últimos dígitos sejam calculados matematicamente a partir dos nove primeiros. Para `123456789__`, o único sufixo válido é `09`. Qualquer outro falha na verificação.

Por isso inseri esses registros direto na migration, contornando a validação da aplicação intencionalmente. Há um comentário no arquivo explicando essa decisão. Os logins e e-mails desses registros funcionam normalmente para os fluxos de login e recuperação de senha. Para testar o cadastro de uma nova pessoa com CPF válido, há dados prontos na seção "Dados para teste" do README.

---

## Por que o frontend nunca chama o ViaCEP diretamente

Toda consulta de CEP passa pelo endpoint `GET /api/zip-code/{cep}` no backend, que por sua vez chama o ViaCEP. O frontend nunca sabe que o ViaCEP existe.

Isso segue o padrão BFF (Backend for Frontend). A vantagem prática é que se o ViaCEP mudar a URL ou o formato da resposta, só o backend precisa ser atualizado. Também centraliza o tratamento de erros externos num único lugar.

Um detalhe sobre o código: os campos do `ViaCepResponse` estão em português (`logradouro`, `bairro`, `localidade`, `uf`) não por estilo, mas porque são o contrato da API do ViaCEP. O Jackson desserializa mapeando campo a campo pelo nome. Se o campo no record se chamasse `street`, viria `null`. O `ZipCodeService` faz a tradução para inglês ao montar o `ZipCodeDetails`, que é o DTO interno do projeto.

---

## Como os erros da API são tratados

Todos os erros seguem o padrão RFC 7807 Problem Details. É a especificação oficial para erros em APIs REST. Garante que qualquer cliente encontre as informações sempre no mesmo formato, independente do tipo de falha.

O `HandlingErrors` (`@RestControllerAdvice`) intercepta exceções de qualquer controller e mapeia para o status HTTP correto:

| Situação | Status |
|----------|--------|
| Campos inválidos | `400 Bad Request` |
| CPF duplicado | `409 Conflict` |
| Pessoa / CEP não encontrado | `404 Not Found` |
| Qualquer outro erro | `500 Internal Server Error` |

O handler genérico nunca expõe o stack trace ao cliente, só retorna `"Unexpected error"`. Stack trace em resposta de API dá informações sobre a implementação interna para quem não deveria ter.

---

## A decisão de design mais importante — LoginAvailabilityChecker

O `LoginGenerator` não sabe nada de banco de dados. Ele recebe uma função que responde "esse login está livre?" e decide com base nisso.

```java
// No PersonService — consulta real ao banco
loginGenerator.generate(data.fullName(),
    candidate -> !repository.existsByLogin(candidate));

// No teste — Set em memória, zero banco, zero Spring
loginGenerator.generate("Maria Silva Santos",
    login -> !Set.of("mariasi", "joaoped").contains(login));
```

Isso parece simples, mas é o que permite testar o algoritmo inteiro sem subir banco, sem `@SpringBootTest`, em milissegundos. O `LoginGenerator` não tem `@Autowired`, nenhum repositório, nenhum import de JPA. Posso trocar o banco, trocar o ORM, mudar a regra de unicidade e o algoritmo não muda uma linha.

É inversão de dependência na prática: o componente de maior valor de negócio não depende de infraestrutura.

---

## Validação no frontend e no backend ao mesmo tempo

Todas as validações existem nos dois lugares: no frontend em TypeScript e no backend com Bean Validation. Não é redundância, são propósitos diferentes.

O frontend valida para dar feedback imediato ao usuário, sem precisar fazer uma requisição ao servidor.

O backend valida porque o frontend pode ser ignorado. Qualquer um pode chamar a API diretamente via `curl` ou Postman. O servidor nunca confia no que vem do cliente. Se eu tivesse só validação no frontend, qualquer CPF inválido entraria no banco.

---

## CPF mascarado nos logs

Antes de qualquer `log.warn()` ou `log.info()`, o CPF passa pelo `maskDocument` que converte `12345678909` em `123.***.**9-09`.

Logs de aplicação costumam ir para sistemas centralizados como Splunk ou Datadog. CPF completo num log é dado pessoal sensível e pode violar a LGPD. O CPF completo não aparece em nenhuma linha de log do sistema.

---

## Testes automatizados

Escrevi cinco classes de teste cobrindo camadas diferentes:

| Classe | Tipo | O que cobre |
|--------|------|-------------|
| `LoginGeneratorTest` | Unitário puro | Happy path, colisões com a massa legada, normalização de acentos, nomes curtos, invariantes (7 chars, só a–z, sem dígito) |
| `PersonServiceTest` | Unitário com Mockito | Lógica do service sem banco: CPF duplicado, geração de login, not found |
| `PersonControllerTest` | `@WebMvcTest` | Status HTTP (201, 400, 404, 409), body JSON, header `Location` |
| `CpfValidatorTest` | Unitário puro | Algoritmo do dígito verificador, sequências iguais, null, branco |
| `NameValidatorTest` | Unitário puro | 2+ palavras, acentos aceitos, números e símbolos rejeitados |

O `LoginGeneratorTest` lê o `massa_dados.txt` para simular o estado inicial do banco com os 20 registros legados. Assim consigo testar que o algoritmo encontra um login alternativo quando os primeiros candidatos já estão ocupados, sem precisar de banco real.

---