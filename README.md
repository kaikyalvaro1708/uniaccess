<div align="center">

<br/>

<img src="frontend/src/img/logomarca_uniaccess.png" alt="UniAccess" width="75%"/>

<br/>

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
![Docker](https://img.shields.io/badge/Docker-Compose-white?style=for-the-badge&logo=docker&logoColor=white&labelColor=003B71)

</div>

---

## Sumário

- [Sobre o projeto](#sobre-o-projeto)
- [Arquitetura e comunicação](#arquitetura-e-comunicação)
- [Como rodar](#como-rodar)
- [Endpoints](#endpoints)
- [Validações](#validações)
- [Segurança e boas práticas](#segurança-e-boas-práticas)
- [Geração automática de login](#geração-automática-de-login)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Tech stack](#tech-stack)

### Documentação adicional

| Documento                                | Descrição                          |
| ---------------------------------------- | ---------------------------------- |
| [docs/CASE.md](docs/CASE.md)             | Enunciado original do case técnico |
| [docs/EVIDENCIAS.md](docs/EVIDENCIAS.md) | Evidências de testes com prints    |

---

## Sobre o projeto

Aplicação **full stack** de cadastro de pessoas com geração automática de login. O sistema conta com uma interface web (**UniAccess**) construída em React e um backend REST em Spring Boot. O sistema:

- Valida todos os campos de entrada (nome, CPF com dígito verificador, e-mail, data de nascimento, CEP)
- Consulta o endereço automaticamente via ViaCEP (padrão BFF — o frontend nunca chama o ViaCEP diretamente)
- Persiste os dados em PostgreSQL com migrations versionadas (Flyway)
- **Gera automaticamente um login único** a partir do nome da pessoa
- Retorna os dados cadastrados com o login gerado

---

## Arquitetura e comunicação

```
┌─────────────────────────────────────────────────────────────────┐
│                        Docker network                           │
│                                                                 │
│  ┌──────────────┐   /api/*   ┌──────────────┐   JDBC   ┌─────┐ │
│  │   Frontend   │ ─────────▶ │   Backend    │ ────────▶ │  DB │ │
│  │  nginx :80   │            │ Spring Boot  │           │ PG  │ │
│  └──────────────┘            │    :8080     │           └─────┘ │
│         ▲                    └──────┬───────┘             :5432 │
└─────────┼──────────────────────────┼─────────────────────────┘ │
          │                          │ ViaCEP (externo)
     :5173 (host)               :8080 (host)
```

### Frontend → Backend

O React nunca chama o backend diretamente pelo IP. Todas as requisições são feitas para caminhos relativos (`/api/persons`, `/api/zip-code/...`):

- **Em desenvolvimento** (Vite): o `vite.config.ts` tem um proxy que redireciona `/api/*` para `http://localhost:8080`.
- **Em produção / Docker**: o nginx faz esse mesmo papel — recebe `/api/*` e faz proxy para `http://backend:8080/api/*` dentro da rede Docker.

Isso significa que o endereço do backend **nunca aparece no código-fonte do frontend**.

### Backend → Banco de dados

O Spring Boot conecta ao PostgreSQL via JDBC. A URL de conexão muda conforme o ambiente:

| Ambiente    | URL                                                      |
| ----------- | -------------------------------------------------------- |
| Local (dev) | `jdbc:postgresql://localhost:5433/identity_provisioning` |
| Docker      | `jdbc:postgresql://postgres:5432/identity_provisioning`  |

No Docker, o `docker-compose.yml` injeta a URL correta via variável de ambiente (`SPRING_DATASOURCE_URL`), sobrescrevendo o `application.properties` sem precisar alterar nenhum arquivo.

O **Flyway** roda automaticamente ao iniciar o backend e aplica as migrations pendentes (`V1__create_person_table.sql`, `V2__insert_legacy_persons.sql`).

### O papel do Docker

O Docker Compose cria uma **rede privada isolada** onde os serviços se enxergam pelo nome (`postgres`, `backend`, `frontend`). Do ponto de vista do host, apenas as portas mapeadas ficam acessíveis:

| Serviço          | Porta interna | Porta no host |
| ---------------- | ------------- | ------------- |
| PostgreSQL       | 5432          | 5433          |
| Backend          | 8080          | 8080          |
| Frontend (nginx) | 80            | 5173          |

A ordem de inicialização é garantida pelo `depends_on` com `healthcheck` no postgres — o backend só sobe depois que o banco está pronto para aceitar conexões.

---

## Como rodar

### ▶ Com Docker (recomendado)

Pré-requisito: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
docker-compose up --build
```

Esse único comando sobe os três serviços em ordem:

1. **PostgreSQL** — aguarda o healthcheck antes de liberar o backend
2. **Backend** — builda o JAR, roda as migrations Flyway automaticamente
3. **Frontend** — builda o React/Vite e serve via nginx

| O quê             | URL                                   |
| ----------------- | ------------------------------------- |
| **Interface web** | http://localhost:5173                 |
| Swagger UI        | http://localhost:8080/swagger-ui      |
| Health            | http://localhost:8080/actuator/health |

> **Primeiro build:** o Maven baixa todas as dependências do zero — pode levar 3–5 minutos. Os builds seguintes usam cache e são muito mais rápidos.
>
> **Da segunda vez:** `docker-compose up` (sem `--build`) aproveita o cache integralmente.

---

### 🛠 Rodando localmente (modo dev)

Pré-requisitos: Java 21 · Maven · Node.js 22+ · PostgreSQL 16+

**1. Sobe o banco**

```bash
docker-compose up -d postgres
```

**2. Sobe o backend**

```bash
cd backend/identity-provisioning-api
mvn spring-boot:run
```

**3. Sobe o frontend**

```bash
cd frontend
npm install
npm run dev
```

As chamadas à API são proxiadas automaticamente pelo Vite para `localhost:8080`.

---

## Endpoints

### `POST /api/persons` — cadastrar pessoa

```json
{
  "fullName": "João Pedro Silva",
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
  "fullName": "João Pedro Silva",
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

### `GET /api/persons/login/{login}` — buscar pelo login

```
GET /api/persons/login/mariasi
```

### `GET /api/persons/email/{email}` — recuperar login pelo e-mail

Usado no fluxo "Esqueci meu login" — retorna os dados da pessoa (incluindo o login gerado) a partir do e-mail cadastrado.

```
GET /api/persons/email/maria@email.com
```

Resposta `200 OK`:

```json
{
  "id": 1,
  "fullName": "Maria Silva Santos",
  "document": "52998224725",
  "email": "maria@email.com",
  "dateOfBirth": "1998-03-14",
  "login": "mariasn",
  "createdAt": "2026-06-02T10:00:00"
}
```

### `DELETE /api/persons/{id}` — remover

---

## Validações

| Campo                                     | Regras                                                                       |
| ----------------------------------------- | ---------------------------------------------------------------------------- |
| `fullName`                                | Obrigatório, mínimo 2 palavras, apenas letras (acentos e cedilha aceitos, sem números ou símbolos) |
| `document`                                | Obrigatório, CPF com dígito verificador válido                               |
| `email`                                   | Obrigatório, formato válido                                                  |
| `dateOfBirth`                             | Obrigatório, não pode ser futura                                             |
| `zipCode`                                 | Obrigatório, formato `XXXXX-XXX` ou `XXXXXXXX`                               |
| `street`, `neighborhood`, `city`, `state` | Obrigatórios                                                                 |

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

| Nome armazenado       | Login gerado |
| --------------------- | ------------ |
| Maria Silva Santos    | `mariasi`    |
| João Pedro Silva      | `joaoped`    |
| Ana Clara Souza       | `anaclar`    |
| Carlos Eduardo Lima   | `carlose`    |
| Conceição Araújo Lima | `conceic`    |

> O sistema aceita nomes com acentos, cedilha e til — armazenados como digitados. A normalização ocorre **internamente** apenas para gerar o login.

### Como funciona

1. **Normaliza** o nome internamente: remove acentos/cedilha via NFD, minúsculo, mantém só `[a-z]` — apenas para geração do login, sem alterar o dado armazenado
2. **Candidato 1** — primeiro nome + início do sobrenome até 7 chars: `maria + si → mariasi`
3. **Candidatos seguintes** — avança progressivamente nas letras dos sobrenomes
4. **Inverte prioridade** — usa o último sobrenome como base
5. **Fallback** — janela deslizante sobre o pool de letras do nome completo
6. Retorna o **primeiro candidato disponível** — verificado contra o banco via `LoginAvailabilityChecker`

A unicidade é garantida em dois níveis: checagem em código antes de salvar + `UNIQUE constraint` no banco (proteção contra concorrência).

### Testado com TDD

```bash
cd backend/identity-provisioning-api
mvn test -Dtest=LoginGeneratorTest
```

Cobre: happy path, colisão com dados reais da massa, normalização de acentos/cedilha, nomes curtos e as três invariantes (7 chars, só a–z, sem dígito).

---

## Estrutura do projeto

```
case-itau/
├── .github/workflows/ci.yml          # Pipeline CI (backend + frontend)
├── frontend/                         # React 19 + Vite + Tailwind
│   └── src/
│       ├── components/               # LeftPanel, PersonForm, LoginForm, RecoverLoginForm…
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

| Camada          | Tecnologia                                      |
| --------------- | ----------------------------------------------- |
| Linguagem       | Java 21                                         |
| Framework       | Spring Boot 3.4.4                               |
| Persistência    | Spring Data JPA + PostgreSQL 16                 |
| Migrations      | Flyway                                          |
| Validação       | Bean Validation + validators customizados       |
| Documentação    | SpringDoc OpenAPI (Swagger)                     |
| Observabilidade | Spring Boot Actuator                            |
| Banco local     | Docker Compose                                  |
| Testes          | JUnit 5                                         |
| Build           | Maven                                           |
| **Frontend**    | React 19 + TypeScript + Vite + Tailwind CSS     |
| **CI**          | GitHub Actions (compile · tests · lint · build) |
