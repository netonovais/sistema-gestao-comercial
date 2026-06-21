# SGC — Sistema de Gestão Comercial

Sistema de gestão comercial para pequenos negócios, com cadastro de
clientes e produtos, registro de vendas, controle de estoque e
relatórios — desenvolvido como projeto acadêmico.

O projeto é dividido em **dois módulos** que se comunicam via API REST:

```
sistema-gestao-comercial/
├── src/                  → Backend (API REST — Spring Boot)
├── pom.xml
├── desktop/               → Frontend (Interface Desktop — Swing)
│   ├── src/
│   └── pom.xml
└── docs/                  → Documentação técnica (opcional, se adicionada)
```

---

## Arquitetura

```
Apresentação (Swing — projeto desktop/)
        ↓  HTTP / JSON
Controller   →  br.com.sgc.controller
        ↓
Service      →  br.com.sgc.service
        ↓
Domain       →  br.com.sgc.domain.model
        ↓
Repository   →  br.com.sgc.domain.repository
        ↓
MySQL Database
```

A interface desktop **não acessa o banco diretamente** — toda
comunicação acontece via requisições HTTP autenticadas (JWT) para a
API REST.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 3.2.5, Spring Data JPA, Spring Security |
| Banco de dados | MySQL 8+ |
| Autenticação | JWT (jjwt 0.12.5), BCrypt |
| Documentação da API | SpringDoc OpenAPI (Swagger UI) |
| Frontend | Java Swing, java.net.http.HttpClient, Jackson |
| Build | Maven |
| Testes | JUnit 5, Mockito |

---

## Design Pattern Aplicado

**DTO (Data Transfer Object)** — aplicado em `br.com.sgc.dto` (backend)
e espelhado em `br.com.sgc.desktop.model` (frontend), com conversão
centralizada em `br.com.sgc.util.MapperUtil`.

**Motivo:** isolar a camada de domínio (entidades JPA) da camada de
apresentação, evitando expor a estrutura interna do banco diretamente
na API REST e controlando exatamente quais dados trafegam entre
cliente e servidor.

---

## Funcionalidades

- **Autenticação JWT** com perfis `ADMIN` e `FUNCIONARIO`
- **Gestão de Clientes** — CRUD completo, com validação de CPF e e-mail únicos
- **Gestão de Produtos** — CRUD completo, com controle de estoque mínimo
- **Registro de Vendas** — cálculo automático do total, baixa automática de estoque
- **Relatórios** — vendas por período (gráfico anual) e vendas por cliente
- **Controle de acesso por perfil** — exclusões restritas a `ADMIN`

---

## Como Executar

### 1. Pré-requisitos

- Java 21+
- MySQL 8+ em execução
- Maven 3.9+ (ou uma IDE com suporte a Maven, como Eclipse/STS)

### 2. Banco de Dados

```sql
CREATE DATABASE sgc_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Backend (API REST)

Edite `src/main/resources/application.yml` com as credenciais do seu MySQL:

```yaml
spring:
  datasource:
    username: root
    password: SUA_SENHA_AQUI
```

Execute:

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Documentação interativa
disponível em `http://localhost:8080/swagger-ui.html`.

As tabelas são criadas automaticamente (Hibernate `ddl-auto: update`),
e dois usuários padrão são inseridos automaticamente via `data.sql`:

| Usuário | Senha | Perfil |
|---|---|---|
| admin | admin123 | ADMIN |
| funcionario | func123 | FUNCIONARIO |

### 4. Frontend (Interface Desktop)

Com o backend já em execução, importe a pasta `desktop/` como um
projeto Maven separado na sua IDE e execute a classe
`br.com.sgc.desktop.DesktopApplication`.

Na tela de login, informe o endereço do servidor
(`http://localhost:8080`), usuário e senha.

> Instruções detalhadas em [`desktop/README.md`](desktop/README.md).

---

## Endpoints Principais da API

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | /auth/login | Login e geração de token JWT | Público |
| GET / POST / PUT | /clientes | CRUD de clientes | Autenticado |
| DELETE | /clientes/{id} | Remove cliente | ADMIN |
| GET / POST / PUT | /produtos | CRUD de produtos | Autenticado |
| DELETE | /produtos/{id} | Remove produto | ADMIN |
| GET | /vendas | Lista vendas (filtros: clienteId, período) | Autenticado |
| POST | /vendas | Registra nova venda | Autenticado |

Documentação completa e testável em `/swagger-ui.html`.

---

## Regras de Negócio

- CPF e e-mail do cliente não podem ser duplicados
- Cliente com vendas registradas não pode ser removido
- Preço do produto não pode ser negativo ou zero
- Venda é bloqueada se o estoque do produto for insuficiente
- Venda não pode ser registrada sem itens
- Valor total da venda é calculado automaticamente pelo servidor (nunca confiado ao cliente)
- Estoque é decrementado automaticamente após cada venda confirmada
- Senhas são armazenadas com hash BCrypt
- Token JWT expira em 24 horas

---

## Documentação Técnica

A documentação completa do projeto — incluindo Casos de Uso, Diagrama
de Domínio, Diagrama de Classes, Diagrama Lógico do Banco de Dados e
Manual do Usuário — está disponível no arquivo
`SGC_Documentacao_Tecnica.docx` na raiz do repositório.

---

## Testes

Testes unitários (JUnit + Mockito) cobrindo as regras de negócio
principais dos Services:

```bash
mvn test
```

---

## Estrutura do Backend

```
src/main/java/br/com/sgc/
├── SgcApplication.java
├── config/          → JWT, Spring Security, OpenAPI
├── controller/       → Endpoints REST
├── service/          → Regras de negócio
├── domain/
│   ├── model/         → Entidades JPA
│   ├── repository/    → Spring Data JPA
│   └── enums/
├── dto/               → Data Transfer Objects
├── exception/         → Tratamento global de exceções
└── util/               → MapperUtil (padrão DTO)
```

## Estrutura do Frontend (desktop/)

```
desktop/src/main/java/br/com/sgc/desktop/
├── DesktopApplication.java
├── ui/                → Telas Swing (Login, Clientes, Produtos, Vendas, Relatórios)
├── api/                → Cliente HTTP que consome a API REST
├── model/              → DTOs espelhados do backend
└── session/             → Gerenciamento do token JWT e usuário logado
```

---

## Equipe

[Adicionar nomes dos integrantes do grupo]

## Repositório

https://github.com/netonovais/sistema-gestao-comercial
