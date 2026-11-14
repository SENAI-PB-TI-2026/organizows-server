# Organizows Server

Servidor backend do projeto **Organizows**.

## Sobre

Este repositório contém a API backend responsável pelos serviços e regras de negócio da aplicação Organizows (desenvolvido em Spring Boot com Java 25).

## Documentação da API (OpenAPI / Swagger)

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
| `PUT` | `/api/v1/localizacoes/{id}` | Atualiza dados cadastrais e vínculo de pai da localização | `200 OK` |
| `DELETE` | `/api/v1/localizacoes/{id}` | Remove uma localização pelo seu identificador | `204 No Content` |

---

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

### Executar Testes
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

---

## Perfis de Ambiente e Segurança de Credenciais

- **Perfil Padrão (`h2`):** Configurado como perfil default via `spring.profiles.default=h2`. Permite que novos desenvolvedores clonem o repositório e iniciem imediatamente a aplicação e testes com banco em memória H2 e migrações Flyway aplicadas automaticamente sem dependências externas.
- **Perfis de Homologação/Produção:** O ambiente externo pode definir `SPRING_PROFILES_ACTIVE=prod` e injetar credenciais e chaves via variáveis de ambiente (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
- **Segurança de Segredos:** Nenhuma senha, token ou chave de API deve ser versionada no repositório. O arquivo `.gitignore` já bloqueia `.env`, arquivos de credenciais e certificados.

---

## Guia de Contribuição e Rastreabilidade

Para manter a rastreabilidade entre tarefas, commits e entregas avaliadas, todos os colaboradores devem seguir as diretrizes documentadas em nosso [Guia de Contribuição (CONTRIBUTING.md)](CONTRIBUTING.md):

* **Conventional Commits com Chave Scrum**: todo commit deve conter a chave do ticket, e.g. `feat(escopo): [SCRUM-N] descricao`.
* **Revisão Cruzada Obrigatória**: todo merge em `develop` exige Pull Request revisado e aprovado por outro integrante.
* **Evidências de Teste**: todo Pull Request deve incluir link da tarefa, descrição e evidência básica de testes.

Consulte o [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes completos e exemplos.

## Licença

Ainda não definida.

