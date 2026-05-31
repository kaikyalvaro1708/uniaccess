<div align="center">

<br/>

# Identity Provisioning API

**Desafio Técnico — Engenharia de Software Jr.**

<br/>

![Java](https://img.shields.io/badge/Java-21-white?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=003B71)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-white?style=for-the-badge&logo=springboot&logoColor=white&labelColor=003B71)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-white?style=for-the-badge&logo=postgresql&logoColor=white&labelColor=003B71)
![Maven](https://img.shields.io/badge/Maven-Build-white?style=for-the-badge&logo=apachemaven&logoColor=white&labelColor=003B71)
![JUnit 5](https://img.shields.io/badge/JUnit-5-white?style=for-the-badge&logo=junit5&logoColor=white&labelColor=003B71)
![React](https://img.shields.io/badge/React-19-white?style=for-the-badge&logo=react&logoColor=white&labelColor=003B71)
![TypeScript](https://img.shields.io/badge/TypeScript-5-white?style=for-the-badge&logo=typescript&logoColor=white&labelColor=003B71)
![Vite](https://img.shields.io/badge/Vite-8-white?style=for-the-badge&logo=vite&logoColor=white&labelColor=003B71)

</div>

---

## Sobre o projeto

Aplicação **full stack** de cadastro de pessoas com geração automática de login. O sistema conta com uma interface web (**UniAccess**) construída em React e um backend REST em Spring Boot. O sistema:

- Valida todos os campos de entrada (nome, CPF com dígito verificador, e-mail, data de nascimento, CEP)
- Consulta o endereço automaticamente via ViaCEP (padrão BFF — o frontend nunca chama o ViaCEP diretamente)
- Persiste os dados em PostgreSQL com migrations versionadas (Flyway)
- **Gera automaticamente um login único** a partir do nome da pessoa
- Retorna os dados cadastrados com o login gerado

---

## Como rodar

### Pré-requisitos

- Docker Desktop
- Java 21
- Maven
- Node.js 22+

### 1. Sobe o banco

```bash
docker-compose up -d
```

### 2. Sobe a API

```bash
cd backend/identity-provisioning-api
mvn spring-boot:run
```

O Flyway roda automaticamente ao iniciar — cria a tabela e carrega os 20 registros legados.

### 3. Sobe o Frontend

```bash
cd frontend
npm install
npm run dev
```

As chamadas à API são proxiadas automaticamente para o backend via Vite.

### 4. Acessa

| O quê | URL |
|---|---|
| **Interface web** | http://localhost:5173 |
| Swagger | http://localhost:8080/swagger-ui |
| Health | http://localhost:8080/actuator/health |

---

## Endpoints

### `POST /api/persons` — cadastrar pessoa

```json
{
  "fullName": "Joao Pedro Silva",
  "document": "529.982.247-25",
  "email": "joao.pedro@email.com",
  "dateOfBirth": "1998-05-09",
  "zipCode": "01310-100",
  "street": "Avenida Paulista",
  "neighborhood": "Bela Vista",
  "city": "São Paulo",
  "state": "SP",
  "complement": "Apto 42"
}
```

Resposta `201 Created`:

```json
{
  "id": 21,
  "fullName": "Joao Pedro Silva",
  "document": "529.982.247-25",
  "email": "joao.pedro@email.com",
  "dateOfBirth": "1998-05-09",
  "login": "joaoped",
  "createdAt": "2026-05-31T10:00:00"
}
```

### `GET /api/zip-code/{zipCode}` — buscar endereço pelo CEP

```
GET /api/zip-code/01310-100
```

```json
{
  "zipCode": "01310-100",
  "street": "Avenida Paulista",
  "neighborhood": "Bela Vista",
  "city": "São Paulo",
  "state": "SP"
}
```

### `GET /api/persons` — listar pessoas (paginado)

```
GET /api/persons?page=0&size=10&sort=fullName,asc
```

### `GET /api/persons/{id}` — buscar por id

### `DELETE /api/persons/{id}` — remover

---

## Validações

| Campo | Regras |
|---|---|
| `fullName` | Obrigatório, apenas letras e espaços (sem acento/cedilha), mínimo 2 palavras |
| `document` | Obrigatório, CPF com dígito verificador válido |
| `email` | Obrigatório, formato válido |
| `dateOfBirth` | Obrigatório, não pode ser futura |
| `zipCode` | Obrigatório, formato `XXXXX-XXX` ou `XXXXXXXX` |
| `street`, `neighborhood`, `city`, `state` | Obrigatórios |

Erros retornam no padrão **RFC 7807 Problem Details**:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "errors": ["must be a valid CPF", "must not be blank"]
}
```

---

## Segurança e boas práticas

- CPF nunca aparece em texto puro nos logs — mascarado como `123.***.**9-01`
- Stack trace nunca é exposto ao cliente
- Queries via JPA/ORM parametrizadas (sem SQL dinâmico)
- Dados legados da massa de testes inseridos via migration, com comentário explicando a decisão de não revalidar CPFs históricos

---

## Geração automática de login

Quando uma pessoa é cadastrada, o sistema gera automaticamente um **login de exatamente 7 letras minúsculas (a–z)** derivado do nome.

| Nome | Login gerado |
|---|---|
| Maria Silva Santos | `mariasi` |
| Joao Pedro Silva | `joaoped` |
| Ana Clara Souza | `anaclar` |
| Carlos Eduardo Lima | `carlose` |

### Como funciona

1. **Normaliza** o nome: remove acentos/cedilha via NFD, minúsculo, mantém só `[a-z]`
2. **Candidato 1** — primeiro nome + início do sobrenome até 7 chars: `maria + si → mariasi`
3. **Candidatos seguintes** — avança progressivamente nas letras dos sobrenomes
4. **Inverte prioridade** — usa o último sobrenome como base
5. **Fallback** — janela deslizante sobre o pool de letras do nome completo
6. Retorna o **primeiro candidato disponível** — verificado contra o banco via `LoginAvailabilityChecker`

A unicidade é garantida em dois níveis: checagem em código antes de salvar + `UNIQUE constraint` no banco (proteção contra concorrência).

### Testado com TDD

```bash
cd backend/identity-provisioning-api
JAVA_HOME="/c/Program Files/Java/jdk-21" mvn test -Dtest=LoginGeneratorTest
```

Cobre: happy path, colisão com dados reais da massa, normalização de acentos/cedilha, nomes curtos e as três invariantes (7 chars, só a–z, sem dígito).

---

## Estrutura do projeto

```
case-itau/
├── .github/workflows/ci.yml          # Pipeline CI (backend + frontend)
├── frontend/                         # React 19 + Vite + Tailwind
│   └── src/
│       ├── components/               # LeftPanel, PersonForm, LoginForm…
│       ├── services/api.ts           # Chamadas ao backend
│       ├── types/
│       └── utils/
└── backend/identity-provisioning-api/
└── src/main/java/com/itau/identityprovisioning/
    ├── controller/
    │   ├── PersonController.java
    │   └── ZipCodeController.java
    ├── domain/
    │   ├── person/
    │   │   ├── Person.java
    │   │   ├── PersonRepository.java
    │   │   ├── PersonService.java
    │   │   ├── RegisterPersonData.java
    │   │   ├── PersonDetailsData.java
    │   │   ├── PersonSummaryData.java
    │   │   └── validation/
    │   │       ├── ValidName / NameValidator
    │   │       └── ValidCpf  / CpfValidator
    │   └── zipcode/
    │       ├── ViaCepResponse.java
    │       ├── ZipCodeDetails.java
    │       └── ZipCodeService.java
    ├── infra/
    │   ├── exception/
    │   │   ├── HandlingErrors.java
    │   │   ├── DocumentAlreadyExistsException.java
    │   │   ├── PersonNotFoundException.java
    │   │   └── ZipCodeNotFoundException.java
    │   └── http/
    │       └── RestClientConfig.java
    └── login/
        ├── LoginAvailabilityChecker.java
        ├── LoginGenerator.java
        └── LoginGeneratorDemo.java

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__create_person_table.sql
    └── V2__insert_legacy_persons.sql
```

---

## Tech stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Persistência | Spring Data JPA + PostgreSQL 16 |
| Migrations | Flyway |
| Validação | Bean Validation + validators customizados |
| Documentação | SpringDoc OpenAPI (Swagger) |
| Observabilidade | Spring Boot Actuator |
| Banco local | Docker Compose |
| Testes | JUnit 5 |
| Build | Maven |
| **Frontend** | React 19 + TypeScript + Vite + Tailwind CSS |
| **CI** | GitHub Actions (compile · tests · lint · build) |
