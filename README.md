<div align="center">

# 🎲 Dice Roll

### API REST para gerenciamento de mesas de RPG de mesa

Sistema backend que permite criar mesas, gerenciar jogadores, montar fichas personalizadas,
distribuir itens e rolar dados — tudo com autenticação JWT.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](#-licença)

</div>

---

## 📖 Sobre

O **Dice Roll** é uma API REST criada para mestres e jogadores de RPG de mesa. Ele resolve o problema
de gerenciar fichas, regras e rolagens de forma digital e flexível — funcionando para **qualquer sistema**
(D&D, Tormenta, Cyberpunk, Call of Cthulhu...), já que a estrutura da ficha é **100% configurável pelo mestre**.

### ✨ Principais recursos

- 🔐 **Autenticação** com JWT (registro, login e recuperação de senha)
- 🏰 **Mesas** com mestre e jogadores (convites, kick, sair)
- 📋 **Fichas dinâmicas** — o mestre define categorias, campos, atributos e perícias
- ⚙️ **Regras condicionais** — ex: "se Classe = Mago, Inteligência +3"
- 🎲 **Rolagem vinculada** — rola dado + atributo + perícia automaticamente
- 📈 **Evolução de personagem** — mestre concede pontos durante o jogo
- 🎒 **Inventário** — pool de itens na mesa, mestre distribui aos jogadores
- 👹 **NPCs** com PV/CA e controle de dano/cura
- 📦 **Templates prontos** — Fantasia Medieval, Cyberpunk e Horror

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.2 |
| Segurança | Spring Security + JWT (jjwt) |
| Persistência | Spring Data JPA + Hibernate |
| Banco | PostgreSQL 16 (via Docker) |
| Build | Maven |
| Validação | Jakarta Bean Validation |

---

## 🏗️ Arquitetura

```
com.diceroller/
├── config/       → Configurações (Security, beans)
├── controller/   → Endpoints HTTP (REST)
├── service/      → Regras de negócio
├── repository/   → Acesso a dados (Spring Data JPA)
├── domain/       → Entidades JPA (tabelas do banco)
├── dto/          → Objetos de transferência (request/response)
├── security/     → JWT (filtro e serviço)
└── exception/    → Tratamento global de erros
```

**Fluxo:** `Controller → Service → Repository → Domain (PostgreSQL)`

---

## 🚀 Tutorial: como rodar a API

### Pré-requisitos

- **Java 21** (JDK) — [baixar](https://adoptium.net/)
- **Maven** — [baixar](https://maven.apache.org/download.cgi) (ou use o do IntelliJ)
- **Docker** + Docker Compose — [baixar](https://docs.docker.com/get-docker/)
- **Git**

Verifique:
```bash
java -version     # deve mostrar 21
mvn -version
docker --version
```

### 1️⃣ Clonar o repositório

```bash
git clone git@github.com:joaomarcelo-java/dice-roll.git
cd dice-roll
```

### 2️⃣ Configurar variáveis de ambiente

Copie o arquivo de exemplo e edite com seus valores:

```bash
cp .env.example .env
```

Conteúdo do `.env`:
```env
POSTGRES_DB=diceroller_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
```

### 3️⃣ Subir o banco de dados

```bash
docker compose up -d
```

Confirme que o container está rodando:
```bash
docker ps
```

> Deve aparecer o container `diceroller-db` na porta `5432`.

### 4️⃣ Configurar a aplicação

Copie o template de configuração e edite:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edite `application.properties` e **defina uma chave JWT forte**:

```properties
security.jwt.key=SUA-CHAVE-SECRETA-AQUI
```

> 💡 Gere uma chave segura com:
> ```bash
> openssl rand -base64 32
> ```

### 5️⃣ Rodar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em **`http://localhost:8080`**. As tabelas são criadas automaticamente pelo Hibernate.

### 6️⃣ Testar

Faça um cadastro:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mestre",
    "email": "mestre@email.com",
    "password": "Senha@123"
  }'
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "nome": "mestre",
  "email": "mestre@email.com"
}
```

Use o `token` nas próximas requisições no header:
```
Authorization: Bearer <token>
```

---

## 🔌 Endpoints

> Todas as rotas (exceto `register`, `login`, `forgot-password` e `reset-password`) exigem
> o header `Authorization: Bearer <token>`.

### 🔐 Autenticação — `/auth`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Cadastro de usuário |
| `POST` | `/auth/login` | Login (retorna JWT) |
| `POST` | `/auth/forgot-password` | Solicita token de recuperação |
| `POST` | `/auth/reset-password` | Redefine a senha com o token |

### 👤 Usuário

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/me` | Dados do usuário logado |

### 🏰 Mesas — `/api/tables`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tables` | Criar mesa |
| `GET` | `/api/tables` | Listar minhas mesas (mestre + jogador) |
| `GET` | `/api/tables/{id}` | Detalhes da mesa |
| `PUT` | `/api/tables/{id}` | Editar mesa (só mestre) |
| `DELETE` | `/api/tables/{id}` | Deletar mesa (só mestre) |
| `PUT` | `/api/tables/{id}/config` | Definir ficha da mesa (só mestre) |
| `GET` | `/api/tables/{id}/config` | Ver ficha da mesa |
| `POST` | `/api/tables/{id}/items` | Criar item no pool da mesa |
| `GET` | `/api/tables/{id}/items` | Listar itens da mesa (só mestre) |
| `DELETE` | `/api/tables/{id}/items/{itemId}` | Remover item da mesa |

### 👥 Membros — `/api/tables/{id}/members`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tables/{id}/members` | Adicionar jogador (só mestre) |
| `GET` | `/api/tables/{id}/members` | Listar membros |
| `DELETE` | `/api/tables/{id}/members/leave` | Sair da mesa |
| `DELETE` | `/api/tables/{id}/members/{userId}` | Expulsar jogador (só mestre) |

### 📜 Convites

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tables/{id}/invites` | Mestre envia convite |
| `GET` | `/api/invites/pending` | Ver convites pendentes |
| `PATCH` | `/api/invites/{inviteId}` | Aceitar/recusar convite |

### 🧙 Personagens — `/api/tables/{tableId}/characters`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tables/{tableId}/characters` | Criar personagem |
| `GET` | `/api/tables/{tableId}/characters` | Listar personagens |
| `GET` | `/api/tables/{tableId}/characters/{charId}` | Ver personagem |
| `PUT` | `/api/tables/{tableId}/characters/{charId}` | Editar personagem |
| `DELETE` | `/api/tables/{tableId}/characters/{charId}` | Deletar personagem |
| `POST` | `/api/tables/{tableId}/characters/{charId}/evolve` | Conceder pontos (só mestre) |
| `POST` | `/api/tables/{tableId}/characters/{charId}/roll-stats` | Rolar atributos na criação |
| `POST` | `/api/tables/{tableId}/characters/{charId}/roll` | Rolagem vinculada |
| `POST` | `/api/tables/{tableId}/characters/{charId}/items` | Dar item (só mestre) |
| `DELETE` | `/api/tables/{tableId}/characters/{charId}/items` | Remover item (só mestre) |

### 👹 NPCs — `/api/tables/{tableId}/npcs`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/tables/{tableId}/npcs` | Criar NPC (só mestre) |
| `GET` | `/api/tables/{tableId}/npcs` | Listar NPCs |
| `GET` | `/api/tables/{tableId}/npcs/{npcId}` | Ver NPC |
| `PUT` | `/api/tables/{tableId}/npcs/{npcId}` | Editar NPC (só mestre) |
| `DELETE` | `/api/tables/{tableId}/npcs/{npcId}` | Deletar NPC (só mestre) |
| `PATCH` | `/api/tables/{tableId}/npcs/{npcId}/pv` | Aplicar dano/cura |

### 🎲 Rolagem

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/roll` | Rolar dados livres |

### 📦 Templates prontos

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/templates/premade` | Listar templates disponíveis |
| `POST` | `/api/tables/{tableId}/template/from-premade/{templateName}` | Aplicar template na mesa |

---

## 🧩 Exemplo: configurar uma ficha

O mestre define a estrutura da ficha via `PUT /api/tables/{id}/config`:

```json
{
  "categorias": [
    {
      "nome": "Identificação",
      "tipo": "identificacao",
      "metodo": "livre",
      "campos": [
        { "nome": "Personagem", "tipo": "texto" },
        { "nome": "Classe", "tipo": "texto", "opcoes": ["Mago", "Guerreiro"] }
      ]
    },
    {
      "nome": "Atributos",
      "tipo": "atributos",
      "metodo": "pontos",
      "pontosDisponiveis": 27,
      "configuracaoRolagem": { "tipo": "dado", "dado": "d20", "somarValorCampo": true },
      "campos": [
        { "nome": "Força", "tipo": "numero", "min": 3, "max": 18 },
        { "nome": "Inteligência", "tipo": "numero", "min": 3, "max": 18 }
      ]
    }
  ],
  "regras": [
    {
      "condicao": { "campo": "Classe", "valor": "Mago" },
      "efeitos": [
        { "alvo": "Inteligência", "categoria": "Atributos", "modificador": 3 },
        { "alvo": "Força", "categoria": "Atributos", "modificador": -1 }
      ]
    }
  ]
}
```

---

## 🔒 Segurança

- Senhas armazenadas com **hash BCrypt**
- Autenticação **stateless** via **JWT** (expiração de 24h)
- Validação de entrada com Bean Validation (`@Valid`, `@Size`, `@Pattern`...)
- Validação de permissões (mestre vs. jogador) em todas as operações sensíveis
- Segredos (chave JWT, senha do banco) **fora do repositório** — via variáveis de ambiente

> ⚠️ **Nunca** commite `application.properties` nem `.env`. Eles já estão no `.gitignore`.

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

Feito com ☕ e 🎲 por [joaomarcelo-java](https://github.com/joaomarcelo-java)

</div>
