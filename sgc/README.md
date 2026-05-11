# SGC – Sistema de Gestão Comercial

Sistema desenvolvido como projeto acadêmico para gerenciamento de vendas, clientes e produtos.

---

## Tecnologias

| Tecnologia       | Versão  | Função                          |
|------------------|---------|---------------------------------|
| Java             | 21      | Linguagem principal             |
| Spring Boot      | 3.2.5   | Framework backend               |
| Spring Data JPA  | 3.2.5   | Persistência / ORM              |
| Spring Security  | 3.2.5   | Autenticação e autorização      |
| MySQL            | 8+      | Banco de dados                  |
| JWT (jjwt)       | 0.12.5  | Token de autenticação           |
| Lombok           | —       | Redução de boilerplate          |
| SpringDoc OpenAPI| 2.5.0   | Documentação Swagger            |
| Maven            | 3.9+    | Gerenciador de dependências     |

---

## Arquitetura em Camadas

```
Apresentação (Web/Swing)
        ↓
Controller  →  br.com.sgc.controller
        ↓
Service     →  br.com.sgc.service
        ↓
Domain      →  br.com.sgc.domain.model
        ↓
Repository  →  br.com.sgc.domain.repository
        ↓
MySQL Database
```

---

## Design Pattern Aplicado

**DTO (Data Transfer Object)**  
Aplicado em: `br.com.sgc.dto` + `br.com.sgc.util.MapperUtil`  
Motivo: Isolar a camada de domínio da API REST, evitando expor entidades JPA diretamente.

---

## Endpoints da API

### Autenticação
| Método | Endpoint      | Descrição           | Auth |
|--------|---------------|---------------------|------|
| POST   | /auth/login   | Login e geração JWT | ❌   |

### Clientes
| Método | Endpoint         | Descrição              | Perfil       |
|--------|------------------|------------------------|--------------|
| GET    | /clientes        | Lista todos            | ADMIN/FUNC   |
| GET    | /clientes/{id}   | Busca por ID           | ADMIN/FUNC   |
| POST   | /clientes        | Cria cliente           | ADMIN/FUNC   |
| PUT    | /clientes/{id}   | Atualiza cliente       | ADMIN/FUNC   |
| DELETE | /clientes/{id}   | Remove cliente         | ADMIN only   |

### Produtos
| Método | Endpoint                | Descrição              | Perfil       |
|--------|-------------------------|------------------------|--------------|
| GET    | /produtos               | Lista todos            | ADMIN/FUNC   |
| GET    | /produtos/{id}          | Busca por ID           | ADMIN/FUNC   |
| GET    | /produtos/estoque-baixo | Produtos críticos      | ADMIN/FUNC   |
| POST   | /produtos               | Cria produto           | ADMIN/FUNC   |
| PUT    | /produtos/{id}          | Atualiza produto       | ADMIN/FUNC   |
| DELETE | /produtos/{id}          | Remove produto         | ADMIN only   |

### Vendas
| Método | Endpoint      | Descrição              | Perfil     |
|--------|---------------|------------------------|------------|
| GET    | /vendas       | Lista / filtra vendas  | ADMIN/FUNC |
| GET    | /vendas/{id}  | Busca por ID           | ADMIN/FUNC |
| POST   | /vendas       | Registra nova venda    | ADMIN/FUNC |

**Filtros disponíveis em GET /vendas:**
- `?clienteId=1` → vendas de um cliente
- `?inicio=2025-01-01T00:00:00&fim=2025-12-31T23:59:59` → por período

---

## Como Executar

### Pré-requisitos
- Java 21+
- MySQL 8+ rodando
- Maven 3.9+

### 1. Configurar banco de dados
```sql
CREATE DATABASE sgc_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configurar credenciais
Edite `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: seu_usuario
    password: sua_senha
```

### 3. Executar
```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`

### 4. Swagger UI
Acesse: `http://localhost:8080/swagger-ui.html`

---

## Usuários Padrão

| Username     | Senha    | Perfil      |
|--------------|----------|-------------|
| admin        | admin123 | ADMIN       |
| funcionario  | func123  | FUNCIONARIO |

---

## Exemplo de uso da API

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "senha": "admin123"}'
```

### Usar token nas requisições
```bash
curl http://localhost:8080/clientes \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## Estrutura do Projeto

```
src/main/java/br/com/sgc/
├── SgcApplication.java
├── config/          # JWT e Security
├── controller/      # API REST endpoints
├── service/         # Regras de negócio
├── domain/
│   ├── model/       # Entidades JPA
│   ├── repository/  # Spring Data JPA
│   └── enums/
├── dto/             # Data Transfer Objects
├── exception/       # Exceções customizadas
└── util/            # MapperUtil (DTO pattern)
```

---

## Regras de Negócio Implementadas

- CPF não pode ser duplicado
- E-mail deve ser válido e único
- Cliente com vendas não pode ser removido
- Preço do produto não pode ser negativo
- Estoque mínimo é controlado e alertado
- Venda bloqueada se estoque insuficiente
- Venda sem itens não é permitida
- Valor total calculado automaticamente
- Estoque atualizado após cada venda
- Senha criptografada com BCrypt
- Token JWT com expiração de 24h
