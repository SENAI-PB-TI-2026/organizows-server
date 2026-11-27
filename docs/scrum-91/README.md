# SCRUM-91 · Proposta de PostgreSQL para desenvolvimento local

[Cartão no Jira](https://team-senai.atlassian.net/browse/SCRUM-91)

Roteiro para validar o PostgreSQL 17 com Docker Compose, conteúdo ministrado em 19/11/2026. O Compose e o exemplo de configuração agora ficam na raiz do repositório; veja também o [README principal](../../README.md).

## Pré-requisitos

- Java compatível com o projeto e Docker com o plugin Compose v2. Uma instalação local de PostgreSQL também pode ser utilizada para desenvolvimento.
- Porta local 5433 disponível. O Compose publica nela a porta 5432 do contêiner, somente em `127.0.0.1`. Altere `POSTGRES_PORT` no arquivo local se necessário.
- O backend usa o driver JDBC PostgreSQL e o módulo Flyway para PostgreSQL, com versões gerenciadas pelo Spring Boot. [Suporte PostgreSQL no Flyway](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database)

## Configuração local

O Compose recebe `POSTGRES_DB` e `POSTGRES_USER` com valores fictícios de desenvolvimento. Preencha `POSTGRES_PASSWORD` apenas no `.env` local, ignorado pelo Git. O volume nomeado `postgres-data` mantém os dados entre reinicializações. As credenciais do Compose inicializam o banco quando o volume está vazio. [Variáveis no Docker Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation)

O Spring Boot lê as variáveis abaixo do `.env` na **raiz do repositório**, ou do ambiente externo, que tem precedência. Sem essas definições, o perfil `dev` usa H2 em memória. O Compose e o backend compartilham o mesmo `.env` da raiz. Preencha `POSTGRES_PASSWORD` e `DEV_DB_PASSWORD` com a mesma senha local. Mantenha as senhas nos arquivos locais ignorados pelo Git ou no ambiente da IDE.

| Variável do perfil `dev` | Valor de desenvolvimento |
| --- | --- |
| `DEV_DB_URL` | `jdbc:postgresql://127.0.0.1:5433/organizows_dev`, ajustado conforme `POSTGRES_PORT` e `POSTGRES_DB` |
| `DEV_DB_DRIVER` | `org.postgresql.Driver` |
| `DEV_DB_USER` | Usuário do PostgreSQL local, por exemplo `organizows_dev`. Com Compose, use o valor de `POSTGRES_USER`. |
| `DEV_DB_PASSWORD` | Senha do usuário local. Com Compose, use o valor de `POSTGRES_PASSWORD`. |

Para uma instalação local, ajuste host, porta e nome do banco na URL JDBC. Comece com um banco vazio para verificar a aplicação das migrações.

## Roteiro de validação com Docker Compose

Na raiz do repositório, copie o exemplo somente se ainda não houver um `.env`. Edite o arquivo para preencher as duas variáveis de senha com o mesmo valor local. Se já houver um `.env`, complete os campos necessários em vez de sobrescrevê-lo. O nome explícito do projeto permite isolar a validação do ambiente diário.

```powershell
Copy-Item .env.example .env # somente se .env ainda não existir
# Edite .env antes de continuar.
$composeArgs = @('-p', 'organizows-scrum91-validacao')
docker compose @composeArgs config --quiet
docker compose @composeArgs up -d --wait
docker compose @composeArgs ps
docker compose @composeArgs exec -T postgres postgres --version
```

Com o banco disponível, confira as quatro variáveis `DEV_DB_*` no `.env` da raiz ou no ambiente externo e inicie o backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Na primeira execução com um volume novo, registre a conexão JDBC, o resultado das migrações Flyway em base vazia e a inicialização do Hibernate. Consulte `curl.exe -i http://localhost:8080/api/v1/status` em outro terminal.

Para verificar o volume, pare o backend, consulte o histórico do Flyway, recrie apenas o contêiner e repita a consulta antes de reiniciar o backend. Use o nome e o usuário configurados no `.env` do Compose se você alterou os exemplos:

```powershell
docker compose @composeArgs exec -T postgres psql -U organizows_dev -d organizows_dev -tAc "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank"
docker compose @composeArgs ps -q postgres
docker compose @composeArgs stop postgres
docker compose @composeArgs up -d --wait --force-recreate postgres
docker compose @composeArgs ps -q postgres
docker compose @composeArgs exec -T postgres psql -U organizows_dev -d organizows_dev -tAc "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank"
```

Compare as saídas. O ID do contêiner deve mudar e o histórico deve continuar com as mesmas quatro versões. Registre os resultados sem incluir credenciais.

## Verificação manual do hotfix

- **PostgreSQL 14 local:** o Hikari abriu a conexão JDBC em uma instância isolada, o Flyway aplicou V1 a V4 em banco vazio, o Hibernate inicializou com validação do esquema e `GET /api/v1/status` retornou HTTP 200 com `environment: dev`.
- **H2:** na validação de 26/11/2026, o perfil `dev` sem `.env` iniciou com H2 2.4.240 em memória, aplicou V1 a V4 e inicializou o JPA. `GET /api/v1/status` retornou HTTP 200 com `status: UP` e `environment: dev`.

### PostgreSQL 17 via Compose em 26/11/2026

As evidências abaixo registram a configuração anterior à centralização na raiz. O roteiro foi executado no projeto Compose isolado `organizows-scrum91-air`, com volume novo, porta 5433 e credenciais de validação fornecidas pelo ambiente. A aplicação foi iniciada com o perfil `dev` na porta 18091.

| Comando ou verificação | Resultado observado |
| --- | --- |
| `docker compose @composeArgs config --quiet` | Configuração válida |
| `docker compose @composeArgs up -d --wait` | Contêiner saudável |
| `docker compose @composeArgs exec -T postgres postgres --version` | `postgres (PostgreSQL) 17.12 (Debian 17.12-1.pgdg13+1)` |
| Inicialização do backend pelo Maven Wrapper | Hikari abriu uma conexão JDBC com PostgreSQL 17.12. Flyway encontrou o esquema vazio e aplicou V1 a V4. Hibernate inicializou o JPA com `spring.jpa.hibernate.ddl-auto=validate`. |
| `curl.exe -i http://127.0.0.1:18091/api/v1/status` | HTTP 200, `status: UP` e `environment: dev` |

Depois de parar o backend, o histórico foi consultado antes e depois de `docker compose @composeArgs stop postgres` e `docker compose @composeArgs up -d --wait --force-recreate postgres`. O contêiner recriado ficou saudável e montou o mesmo volume `organizows-scrum91-air_postgres-data`.

Consulta executada nos dois contêineres:

```sql
SELECT version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

```text
Antes (ID abreviado c399a0a58293):
1|create table localizacao|t|2026-11-26 19:37:28.980955
2|create table material|t|2026-11-26 19:37:29.042757
3|add quantidade material|t|2026-11-26 19:37:29.088886
4|add material technical attributes|t|2026-11-26 19:37:29.125574

Depois (ID abreviado a97b48a28b9e):
1|create table localizacao|t|2026-11-26 19:37:28.980955
2|create table material|t|2026-11-26 19:37:29.042757
3|add quantidade material|t|2026-11-26 19:37:29.088886
4|add material technical attributes|t|2026-11-26 19:37:29.125574
```
