# Organizows Server

Servidor backend do projeto **Organizows**.

## Sobre

Este repositório contém a API backend responsável pelos serviços e regras de negócio da aplicação Organizows (desenvolvido em Spring Boot com Java 25).

## Documentação da API (OpenAPI / Swagger)

O índice das consultas HTTP do catálogo, com rotas existentes, parâmetros, campos de resposta, exemplos e pendências, está em [docs/consultas-publicas-catalogo.md](docs/consultas-publicas-catalogo.md).

A especificação OpenAPI 3.0 e a interface Swagger UI estão integradas à aplicação para orientar a integração dos módulos das próximas sprints sem necessidade de interfaces visuais no backend:

- **Swagger UI (Documentação Interativa):**
  - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  - [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Especificação OpenAPI (JSON):**
  - [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Especificação OpenAPI (YAML):**
  - [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

---

## Endpoint Técnico de Verificação

Para validação de conectividade, saúde da aplicação e metadados de execução:

### `GET /api/v1/status`

- **Descrição:** Retorna o status operacional do backend, tempo de atividade (uptime), versão e ambiente.
- **Códigos HTTP:**
  - `200 OK`: Serviço ativo e operando normalmente.
  - `500 Internal Server Error`: Falha interna inesperada.
- **Exemplo de Resposta (200 OK):**

```json
{
  "success": true,
  "message": "Serviço operacional",
  "data": {
    "status": "UP",
    "applicationName": "organizows",
    "version": "1.0.0",
    "environment": "development",
    "timestamp": "2026-11-12T21:00:00",
    "uptime": "0 d, 00 h, 15 m, 22 s"
  },
  "timestamp": "2026-11-12T21:00:00"
}
```

---

## Endpoints do Catálogo de Localizações

Gerenciamento do catálogo físico hierárquico (espaços, galpões, salas, armários, prateleiras):

| Método | Endpoint | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/localizacoes` | Cadastra uma nova localização | `201 Created` |
| `GET` | `/api/v1/localizacoes` | Lista todas as localizações (filtro opcional: `?codigo=...`) | `200 OK` |
| `GET` | `/api/v1/localizacoes/raizes` | Lista apenas as localizações de nível superior (sem pai) | `200 OK` |
| `GET` | `/api/v1/localizacoes/{id}` | Busca os detalhes de uma localização por ID | `200 OK` |
| `GET` | `/api/v1/localizacoes/codigo/{codigo}` | Busca os detalhes de uma localização por código | `200 OK` |
| `GET` | `/api/v1/localizacoes/{id}/filhas` | Lista as localizações filhas diretas | `200 OK` |
| `GET` | `/api/v1/localizacoes/{id}/caminho` | Retorna o caminho hierárquico ordenado da raiz até o nó | `200 OK` |
| `GET` | `/api/v1/localizacoes/{id}/impedimentos-exclusao` | Consulta impedimentos para excluir a localização | `200 OK` |
| `PUT` | `/api/v1/localizacoes/{id}` | Atualiza dados cadastrais e vínculo de pai da localização | `200 OK` |
| `DELETE` | `/api/v1/localizacoes/{id}` | Remove uma localização pelo seu identificador | `204 No Content` |

### Consultar impedimentos de exclusão

`GET /api/v1/localizacoes/{id}/impedimentos-exclusao` retorna HTTP `200` para uma localização existente. Exemplo com os dois impedimentos:

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": {
    "podeExcluir": false,
    "motivos": ["POSSUI_LOCALIZACOES_FILHAS", "POSSUI_MATERIAIS_ASSOCIADOS"]
  },
  "timestamp": "2026-11-26T19:00:00"
}
```

- `POSSUI_LOCALIZACOES_FILHAS` indica que há uma localização filha direta.
- `POSSUI_MATERIAIS_ASSOCIADOS` indica que há um material vinculado, inclusive com saldo zero.
- Quando não há vínculos, `podeExcluir` é `true` e `motivos` é `[]`. Um ID inexistente retorna HTTP `404` no formato de erro da API.

O frontend pode desabilitar a ação quando `podeExcluir` for `false` e apresentar os motivos retornados. A consulta é somente leitura e reflete os vínculos observados durante a requisição. O `DELETE` verifica novamente os vínculos e pode retornar HTTP `409` se houver impedimento.

---

## Classificação de materiais

No cadastro e na alteração de classificação, o grupo deve corresponder à categoria. Por exemplo, a categoria `FERRAMENTA` usa o grupo `Ferramentas` e pode usar o subgrupo `Manuais`. O subgrupo é opcional. Consulte os materiais de um grupo com `GET /api/v1/materiais?grupo=Ferramentas`.

Materiais cadastrados antes dessa validação mantêm a classificação original quando são editados sem mudar categoria, grupo ou subgrupo. Para regularizar um cadastro, consulte-o com `GET /api/v1/materiais/{id}` e envie os campos cadastrais no `PUT /api/v1/materiais/{id}` com uma combinação compatível. Os grupos e subgrupos aceitos estão em `CategoriaMaterial.java`. A regularização é feita por material, sem alteração automática dos dados anteriores.

### Consultar classificações permitidas

`GET /api/v1/materiais/classificacoes` retorna HTTP `200` com todas as categorias, inclusive quando ainda não há materiais cadastrados. A consulta usa a mesma configuração do cadastro e da edição e não altera registros.

Exemplo de resposta com uma categoria da lista.

```json
{
  "success": true,
  "data": [
    {
      "categoria": "FERRAMENTA",
      "grupo": "Ferramentas",
      "subgrupos": ["Manuais", "Elétricas"]
    }
  ],
  "timestamp": "2026-11-25T20:00:00"
}
```

Use `categoria` como o identificador estável no formulário. Ao selecionar uma categoria, preencha `grupo` com o valor do item e ofereça somente os valores de `subgrupos`. Envie esses valores com os mesmos nomes de campo no cadastro ou na edição. O subgrupo é opcional e pode ser enviado como `null`.

## Padrão de Respostas e Convenções (Contrato de Integração)

Todas as equipes devem seguir as convenções abaixo ao implementar novos controllers e serviços nas próximas sprints:

### 1. Respostas de Sucesso (`ApiResponse<T>`)
Envelope genérico para respostas bem-sucedidas:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `success` | `boolean` | `true` para sucesso |
| `message` | `string` | Descrição legível da operação |
| `data` | `T` | Objeto ou lista retornada |
| `timestamp` | `string (ISO-8601)` | Data e hora da resposta |

### 2. Respostas de Erro (`ApiErrorResponse`)
Tratamento centralizado pelo `GlobalExceptionHandler`:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `timestamp` | `string (ISO-8601)` | Momento do erro |
| `status` | `int` | Código HTTP (ex: 400, 404, 500) |
| `error` | `string` | Razão do código HTTP (ex: "Bad Request", "Not Found") |
| `message` | `string` | Mensagem explicativa do problema |
| `path` | `string` | URI requisitada |
| `fieldErrors` | `array` *(opcional)* | Lista de campos com falhas de validação (`field`, `message`) |

### 3. Códigos de Status HTTP Convencionados

- `200 OK`: Consulta ou atualização concluída com sucesso.
- `201 Created`: Novo registro criado com sucesso.
- `204 No Content`: Remoção ou operação executada sem corpo de retorno.
- `400 Bad Request`: Requisição inválida ou parâmetros com dados incorretos.
- `401 Unauthorized`: Autenticação ausente ou inválida.
- `403 Forbidden`: Usuário autenticado sem permissão de acesso.
- `404 Not Found`: Recurso não encontrado.
- `500 Internal Server Error`: Erro inesperado do servidor.

---

## Como Executar o Projeto

### Pré-requisitos
- JDK 25 instalado
- Git

### Execução local
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

---

## Perfis de Ambiente e Segurança de Credenciais

- **Perfil Padrão (`h2`):** Configurado como perfil default via `spring.profiles.default=h2`. Permite que novos desenvolvedores clonem o repositório e iniciem imediatamente a aplicação com banco em memória H2 e migrações Flyway aplicadas automaticamente sem dependências externas.
- **Perfil de desenvolvimento (`dev`):** Carrega o arquivo local `.env`, quando presente, e usa H2 em memória por padrão. O Flyway aplica as migrações e o Hibernate valida o esquema.
- **Perfis de Homologação/Produção:** O ambiente externo pode definir `SPRING_PROFILES_ACTIVE=prod` e injetar credenciais e chaves via variáveis de ambiente (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
- **Segurança de Segredos:** Nenhuma senha, token ou chave de API deve ser versionada no repositório. O arquivo `.gitignore` já bloqueia `.env`, arquivos de credenciais e certificados.

### Desenvolvimento com PostgreSQL 17 (Docker Compose)

Pré-requisitos: Java 25, Docker e Docker Compose v2. O Compose na raiz do repositório gerencia somente o banco de dados; o backend continua sendo executado localmente pelo Maven Wrapper.

1. Na raiz do projeto, copie `.env.example` para `.env` (se ainda não existir). Se já tiver um `.env`, complete ou atualize os campos necessários sem perder suas configurações locais.
2. Edite o `.env`: preencha `POSTGRES_PASSWORD` e `DEV_DB_PASSWORD` com a mesma senha local. Use uma senha adequada ao desenvolvimento, sem aspas ou caracteres que exijam escape em arquivos de propriedades.
3. Suba o banco e aguarde o healthcheck:

```powershell
Copy-Item .env.example .env # somente se .env ainda não existir
# Edite .env antes de continuar.
docker compose config --quiet
docker compose up -d --wait
docker compose ps
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

No Linux ou macOS, use `cp .env.example .env` (somente se ainda não existir) e `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

| Variável | Exemplo local |
| --- | --- |
| `POSTGRES_DB` | `organizows_dev` |
| `POSTGRES_USER` | `organizows_dev` |
| `POSTGRES_PASSWORD` | Senha local obrigatória (não versionada) |
| `POSTGRES_PORT` | `5433` (porta no host) |
| `DEV_DB_URL` | `jdbc:postgresql://127.0.0.1:5433/organizows_dev` |
| `DEV_DB_DRIVER` | `org.postgresql.Driver` |
| `DEV_DB_USER` | Mesmo valor de `POSTGRES_USER` |
| `DEV_DB_PASSWORD` | Mesmo valor de `POSTGRES_PASSWORD` |

O PostgreSQL fica acessível somente em `127.0.0.1`. Se alterar porta, nome do banco ou usuário, ajuste também os campos `DEV_DB_*` correspondentes. Para usar PostgreSQL instalado localmente em vez do Compose, configure os quatro campos `DEV_DB_*` conforme essa instalação.

O Flyway aplica automaticamente as migrações pendentes e o Hibernate valida o esquema. Confirme nos logs o perfil `dev`, a conexão PostgreSQL e a inicialização, e consulte [GET /api/v1/status](http://localhost:8080/api/v1/status).

Para encerrar o banco, pare o backend e execute `docker compose down`. O volume nomeado `postgres-data` preserva os dados. Não use `down -v` se deseja mantê-los. As credenciais `POSTGRES_*` inicializam apenas um volume vazio; mudar a senha no arquivo não altera automaticamente a senha de um usuário existente. Quem já utilizava o Compose da SCRUM-91 com um nome de projeto específico (`-p`) deve manter esse nome nos comandos para reutilizar o mesmo volume e evitar criar outro banco.

### Alternativa sem Docker: H2 em memória

Execute diretamente o perfil padrão `h2` (sem copiar o `.env.example` e sem ativar `dev`):

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, use `./mvnw spring-boot:run`. O perfil `dev` também mantém o fallback H2 quando não há `.env` com configuração PostgreSQL nem variáveis de conexão externas. O H2 é volátil e seus dados não são preservados ao encerrar a aplicação.

As variáveis de ambiente do processo têm precedência sobre o `.env` no perfil `dev`. Consulte o [roteiro e evidências da SCRUM-91](docs/scrum-91/README.md) para detalhes da validação do banco.

---

## Guia de Contribuição e Rastreabilidade

Para manter a rastreabilidade entre tarefas, commits e entregas avaliadas, todos os colaboradores devem seguir as diretrizes documentadas em nosso [Guia de Contribuição (CONTRIBUTING.md)](CONTRIBUTING.md):

* **Conventional Commits com Chave Scrum**: todo commit deve conter a chave do ticket, e.g. `feat(escopo): [SCRUM-N] descricao`.
* **Revisão Cruzada Obrigatória**: todo merge em `develop` exige Pull Request revisado e aprovado por outro integrante.
* **Evidências de Teste**: todo Pull Request deve incluir link da tarefa, descrição e evidência da verificação manual.

Consulte o [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes completos e exemplos.

## Licença

Ainda não definida.

