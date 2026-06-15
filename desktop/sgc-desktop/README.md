# SGC Desktop (Swing)

Interface gráfica desktop (Java Swing) do Sistema de Gestão Comercial.
Esta aplicação **consome a API REST** do backend (projeto `sgc`) via HTTP/JSON.

---

## Pré-requisitos

- O **backend** (`sgc`) deve estar **rodando** em `http://localhost:8080`
  (ou outro endereço, que pode ser configurado na tela de login)
- Java 21+
- Maven (para baixar as dependências Jackson)

---

## Telas

| Aba | Funcionalidade |
|---|---|
| **Clientes** | CRUD completo (criar, listar, editar, excluir) |
| **Produtos** | CRUD completo, com destaque visual para estoque baixo |
| **Vendas** | Montagem de venda (cliente + itens), cálculo automático do total, histórico |
| **Relatórios** | Gráfico de vendas mensais por ano + vendas por cliente |

---

## Como executar no Eclipse / STS

1. **File → Import → Maven → Existing Maven Projects**
2. Selecione a pasta `sgc-desktop`
3. Aguarde o Maven baixar as dependências (Jackson)
4. Abra `src/main/java/br/com/sgc/desktop/DesktopApplication.java`
5. Botão direito → **Run As → Java Application**

---

## Login

Na tela inicial, informe:
- **Servidor:** `http://localhost:8080` (endereço do backend)
- **Usuário:** `admin` ou `funcionario`
- **Senha:** `admin123` ou `func123`

---

## Arquitetura

Esta aplicação segue o padrão de **Cliente HTTP / API REST**:

```
Tela (Swing)
    ↓
ApiClient (HTTP + JSON)
    ↓
API REST do backend (Spring Boot)
    ↓
Banco de Dados (MySQL)
```

- `ui/` — Telas Swing (JFrame, JPanel)
- `api/` — Cliente HTTP que consome a API REST (ApiClient)
- `model/` — DTOs que espelham os DTOs do backend
- `session/` — Gerencia o token JWT e dados do usuário logado

A autenticação é feita via **JWT**: o token retornado por `/auth/login`
é armazenado em memória (`SessionManager`) e enviado no header
`Authorization: Bearer <token>` em todas as requisições subsequentes.
