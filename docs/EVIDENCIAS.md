# Evidências de Testes — UniAccess

> Documento de evidências do funcionamento da aplicação.  
> Cobre todos os fluxos exigidos pelo case técnico — prints e testes automatizados.

---

## Sumário

- [1. Ambiente rodando](#1-ambiente-rodando)
- [2. Fluxo de cadastro — sucesso](#2-fluxo-de-cadastro--sucesso)
- [3. Validações — erros no formulário](#3-validações--erros-no-formulário)
- [4. Validação de CPF duplicado](#4-validação-de-cpf-duplicado)
- [5. Consulta automática de CEP](#5-consulta-automática-de-cep)
- [6. Unicidade do login](#6-unicidade-do-login)
- [7. Fluxo de login](#7-fluxo-de-login)
- [8. Recuperação de login por e-mail](#8-recuperação-de-login-por-e-mail)
- [9. Testes automatizados](#9-testes-automatizados)
- [10. API — Swagger UI](#10-api--swagger-ui)

---

## 1. Ambiente rodando

### Docker Compose — todos os serviços no ar

> `docker-compose up --build` executado com sucesso. Três serviços ativos: postgres, backend e frontend.

<!-- Adicione o print abaixo -->
![Docker Compose rodando](./prints/01-docker-compose-up.png)

---

### Interface acessível em localhost:5173

> Tela inicial do UniAccess no browser após `docker-compose up`.

<!-- Adicione o print abaixo -->
![Tela inicial UniAccess](./prints/02-tela-inicial.png)

---

## 2. Fluxo de cadastro — sucesso

### Formulário preenchido com dados válidos

> Todos os campos preenchidos corretamente: nome, CPF, e-mail, data de nascimento, CEP com endereço preenchido automaticamente.

<!-- Adicione o print abaixo -->
![Formulário preenchido](./prints/03-formulario-preenchido.png)

---

### Tela de sucesso com login gerado

> Após envio, o sistema exibe o login único de 7 letras gerado automaticamente a partir do nome.

<!-- Adicione o print abaixo -->
![Cadastro realizado com login gerado](./prints/04-cadastro-sucesso.png)
---

## 3. Validações — erros no formulário

### Nome inválido

> Tentativa de envio com nome contendo números, símbolos ou apenas uma palavra.

<!-- Adicione o print abaixo -->
![Erro de validação — nome inválido](./prints/05-erro-nome-invalido.png)

---

### CPF inválido

> Tentativa de envio com CPF com dígito verificador incorreto ou sequência inválida (ex: 111.111.111-11).

<!-- Adicione o print abaixo -->
![Erro de validação — CPF inválido](./prints/06-erro-cpf-invalido.png)
---

### E-mail inválido

> Tentativa de envio com e-mail sem formato válido.

<!-- Adicione o print abaixo -->
![Erro de validação — e-mail inválido](./prints/07-erro-email-invalido.png)

---

### Data de nascimento futura

> Tentativa de envio com data de nascimento posterior à data atual.

<!-- Adicione o print abaixo -->
![Erro de validação — data futura](./prints/08-erro-data-futura.png)

---

### CEP não encontrado

> Tentativa de consulta com CEP inválido ou inexistente.

<!-- Adicione o print abaixo -->
![Erro de validação — CEP inválido](./prints/09-erro-cep-invalido.png)

---

### Campos obrigatórios vazios

> Tentativa de envio do formulário sem preencher os campos obrigatórios.

<!-- Adicione o print abaixo -->
![Erro de validação — campos vazios](./prints/10-erro-campos-obrigatorios.png)
---

## 4. Validação de CPF duplicado

> Tentativa de cadastro com CPF já existente na base. O sistema retorna erro `409 Conflict` e exibe mensagem ao usuário.

<!-- Adicione o print abaixo -->
![Erro — CPF já cadastrado](./prints/11-erro-cpf-duplicado.png)

---

## 5. Consulta automática de CEP

### CEP digitado — endereço preenchido automaticamente

> Ao sair do campo CEP, os campos de logradouro, bairro, cidade e UF são preenchidos automaticamente via ViaCEP (padrão BFF — o frontend nunca chama o ViaCEP diretamente).

<!-- Adicione o print abaixo -->
![CEP preenchido automaticamente](./prints/12-cep-automatico.png)
---

## 6. Unicidade do login

> Dois cadastros com nomes similares (ex: "Pedro Henrique Alves" e "Pedro Henrique Santos") geram logins **diferentes**, demonstrando o algoritmo de unicidade.

### Primeiro cadastro

<!-- Adicione o print abaixo -->
![Primeiro cadastro — login gerado](./prints/04-cadastro-sucesso.png)

---

### Segundo cadastro com nome similar

<!-- Adicione o print abaixo -->
![Segundo cadastro — login diferente gerado](./prints/14-unicidade-segundo-login.png)
---

## 7. Fluxo de login

### Tela de login

> Usuário acessa a tela de login e digita o login gerado no cadastro.

<!-- Adicione o print abaixo -->
![Tela de login](./prints/15-tela-login.png)

---

### Tela de boas-vindas (logado)

> Após login bem-sucedido, a tela exibe os dados da pessoa com o login utilizado.

<!-- Adicione o print abaixo -->
![Boas-vindas após login](./prints/16-tela-boas-vindas.png)
---

### Login não encontrado

> Tentativa de login com código inexistente na base.

<!-- Adicione o print abaixo -->
![Erro — login não encontrado](./prints/17-erro-login-invalido.png)
---

## 8. Recuperação de login por e-mail

> Fluxo implementado além do escopo mínimo — resolve o caso em que o usuário esquece o login gerado e fica sem acesso.

### Link "Esqueceu o login?" na tela de login

> O link aparece abaixo do formulário de login e leva ao fluxo de recuperação.

### Formulário de recuperação — e-mail digitado

> Usuário informa o e-mail usado no cadastro.

<!-- Adicione o print abaixo -->
![Formulário de recuperação por e-mail](./prints/18-link-esqueci-email-form.png)
---

### Login recuperado com sucesso

> O sistema exibe o login de 7 letras em destaque e o nome da pessoa, com botão para ir direto ao login.

<!-- Adicione o print abaixo -->
![Formulário de recuperação por e-mail](./prints/19-recover-email-form.png)

---

### E-mail não encontrado

> Tentativa de recuperação com e-mail não cadastrado.

<!-- Adicione o print abaixo -->
![Erro — e-mail não encontrado](./prints/21-recover-email-not-found.png)
---

## 9. Testes automatizados

### Suite de testes passando

> Execução de `mvn test` com todos os testes verdes. Cobre o `LoginGenerator` com TDD: happy path, colisões, nomes com acentos, nomes curtos e as três invariantes do login.

<!-- Adicione o print abaixo -->
![Testes automatizados passando](./prints/22-testes-automatizados.png)
---

### CI/GitHub Actions — pipeline verde

> Pipeline rodando no GitHub Actions após push: Backend Compile → Backend Tests → Frontend Lint → Frontend Build.

<!-- Adicione o print abaixo -->
![GitHub Actions — pipeline verde](./prints/23-github-actions.png)
![GitHub Actions — pipeline verde](./prints/23-github-actions-ci.png)
---

## 10. API — Swagger UI

### Documentação interativa dos endpoints

> Swagger UI disponível em `http://localhost:8080/swagger-ui` com todos os endpoints documentados.

<!-- Adicione o print abaixo -->
![Swagger UI](./prints/24-swagger-ui.png)

---

### Endpoint POST /api/persons — testado via Swagger

> Requisição de cadastro executada diretamente pelo Swagger com resposta `201 Created` e login gerado no body.

<!-- Adicione o print abaixo -->
![POST /api/persons via Swagger](./prints/25-swagger-post-persons.png)
---