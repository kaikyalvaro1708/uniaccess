# Case Técnico — Engenharia de Software Jr.

> Documento original recebido por e-mail em 2025.  
> Remetente: Mauricio Felicio  
> Prazo de entrega: **sexta-feira, 05/06/2025**

---

## E-mail de convite

> Olá!
>
> Primeiramente, obrigado por se inscrever na nossa vaga 👋
>
> Ficamos felizes com o seu interesse e queremos te convidar para a próxima etapa do processo: um **case técnico**. Ele foi pensado para avaliarmos um pouco mais do seu raciocínio, organização e forma de construir soluções.
>
> 📅 **Prazo de entrega: até sexta-feira, 05/06**
>
> Todas as instruções do desafio estão logo abaixo neste e-mail.
>
> Caso você decida não seguir no processo, sem problemas 😊 — basta não retornar o case dentro do prazo estabelecido.
>
> Boa sorte no desafio — estamos torcendo por você! 🍀
>
> Abraços,  
> **Mauricio Felicio**

---

## Objetivo

Desenvolver uma aplicação simples de **cadastro de pessoas**, contemplando front-end e back-end, com regras de validação, persistência dos dados e geração automática de login.

O objetivo deste desafio é avaliar conhecimentos de:

- Construção de interfaces
- Consumo e criação de APIs
- Modelagem e persistência de dados
- Validação de campos
- Tratamento de regras de negócio
- Organização do código
- Boas práticas de desenvolvimento

> **Importante:** a solução deve ser desenvolvida utilizando as tecnologias informadas na vaga.

---

## Contexto do desafio

Sua missão será criar uma solução que permita o **cadastro de pessoas** em uma base de dados.

Ao concluir o cadastro com sucesso, o sistema deverá:

- Armazenar as informações da pessoa
- Gerar automaticamente um login
- Garantir que esse login respeite as regras definidas
- Retornar/exibir os dados cadastrados juntamente com o login gerado

---

## Escopo funcional

### 1. Cadastro de pessoa

A aplicação deve permitir o cadastro de uma pessoa com os seguintes **campos obrigatórios**:

| Campo | Observação |
|---|---|
| Nome completo | Obrigatório |
| Documento | A escolha do candidato (CPF, RG, etc.) |
| E-mail | Obrigatório |
| Data de nascimento | Obrigatório |
| CEP | Obrigatório |
| Endereço | Preenchido automaticamente via ViaCEP ou similar |

---

## Regras de validação

### Nome
- Obrigatório
- Deve aceitar apenas conteúdo válido de nome de pessoa
- Recomenda-se remover espaços excedentes no início/fim
- Não pode ser enviado vazio
- **Não pode conter caracteres especiais como til, acentos ou cedilhas**

### Documento
- Obrigatório
- Deve respeitar uma formatação válida definida pelo candidato
- Ex.: CPF, RG, ou outro padrão — desde que a regra fique explicitada
- O candidato deve documentar qual formato foi adotado

### E-mail
- Obrigatório
- Deve respeitar formato válido de e-mail

### Data de nascimento
- Obrigatório
- Deve respeitar formato de data válido
- Deve ser uma data real
- **Não pode ser futura**

### CEP
- Obrigatório
- Deve respeitar formato válido

### Endereço
- Preenchido automaticamente com base na pesquisa de CEP
- Permitir que o usuário complemente os dados

---

## Regra de persistência

Após um cadastro válido, os dados precisam ser armazenados. Opções aceitáveis:

- Banco relacional
- Banco não relacional
- Arquivo local
- Memória persistida de forma simples

> O importante é que exista alguma forma de persistência e que a **escolha seja explicada**.

---

## Regra de geração automática de login

Ao cadastrar uma pessoa, o sistema deve gerar automaticamente um login.

### Regras obrigatórias

O login gerado deve:

- Possuir **exatamente 7 caracteres**
- Ser **único** (não pode se repetir para duas pessoas)
- **Não conter espaços**
- **Não conter números**
- Ser construído utilizando **informações que compõem o nome da pessoa**
- Ser gerado em **caracteres minúsculos**, variando de a–z

### Exemplos ilustrativos

| Nome | Possíveis logins |
|---|---|
| Maria Silva Souza | `mariasi`, `marsouz`, `masilva` |
| João Pedro Lima | `joaoped`, `joalima`, `jpedrol` |

> **Atenção:** o arquivo em anexo deve ser considerado como registros já existentes — o algoritmo deve garantir unicidade considerando essa massa inicial.

### Regra de unicidade

Caso o login inicialmente gerado já exista, o candidato deverá implementar uma estratégia para gerar uma nova variação **sem quebrar as regras**. Estratégias aceitáveis:

- Usar outras combinações possíveis entre nome e sobrenomes
- Avançar na composição de letras do nome
- Trocar a priorização entre nome e sobrenome
- Aplicar nova combinação mantendo 7 letras e sem números

---

## Funcionalidades mínimas esperadas

### Front-end
- Preencher os dados do cadastro
- Validar os campos obrigatórios
- Exibir mensagens de erro quando necessário
- Enviar os dados para o back-end
- Exibir o resultado do cadastro com o login gerado

### Back-end
- Receber os dados do cadastro
- Validar as regras de negócio
- Gerar o login
- Armazenar os dados
- Retornar resposta adequada para alerta, sucesso ou erro

---

## Requisitos técnicos

### Esperado
- Separação entre front-end e back-end
- Código comentado, organizado e legível
- Tratamento de erros
- Validações tanto no client quanto no server
- Persistência funcional
- Documentação mínima para execução do projeto
- Máscaras ou validações de input no front-end

### Diferenciais
- Testes automatizados
- Containerização com Docker
- Documentação de arquitetura
- Boas práticas de API
- Uso de migrations/ORM
- Responsividade da interface
- Deploy ou instruções claras para execução
- **Propor uma identidade visual semelhante à que temos hoje**

---

## Entregáveis

- [ ] Código-fonte do front-end
- [ ] Código-fonte do back-end
- [ ] README com instruções de execução
- [ ] Descrição da lógica de geração do login
- [ ] **Documento de evidências de testes com prints**
- [ ] *(Opcional)* Link da aplicação publicada

---

## Critérios de avaliação

| Critério | O que será avaliado |
|---|---|
| **Funcionamento** | Cadastro funciona? Dados persistidos? Login válido? |
| **Validações** | Campos obrigatórios tratados? Formatações adequadas? Entradas inválidas tratadas? |
| **Qualidade técnica** | Organização, clareza, nomeação, separação de responsabilidades |
| **Back-end** | Estrutura da API, regras de negócio, persistência, tratamento de erros |
| **Front-end** | Usabilidade, feedback ao usuário, validações, clareza visual |
| **Lógica do login** | Respeita as regras? Garante unicidade? É consistente e bem explicada? |
| **Documentação** | Projeto fácil de rodar? README claro? Decisões explicadas? |
