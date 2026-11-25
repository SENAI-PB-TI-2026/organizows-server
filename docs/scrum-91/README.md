# SCRUM-91 · Proposta de PostgreSQL para desenvolvimento local

[Cartão no Jira](https://team-senai.atlassian.net/browse/SCRUM-91)

Proposta para revisão técnica antes da aplicação. A demonstração em execução está prevista para depois da orientação docente na oficina de 04/12/2026.

## Pré-requisitos

- Docker com o plugin Compose v2 e Java compatível com o projeto.
- Porta local 5433 disponível. O Compose publica nela a porta 5432 do contêiner, somente em `127.0.0.1`. Altere `POSTGRES_PORT` no arquivo local se necessário.
- A `develop` ainda precisa das dependências Maven `org.postgresql:postgresql` e `org.flywaydb:flyway-database-postgresql` antes do teste com o perfil `dev`. A inclusão delas faz parte da aplicação da proposta após revisão. [Suporte PostgreSQL no Flyway](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database)

## Configuração local

O Compose recebe `POSTGRES_DB` e `POSTGRES_USER` com valores fictícios de desenvolvimento. Preencha `POSTGRES_PASSWORD` apenas no `.env` local, ignorado pelo Git. O volume nomeado `postgres-data` mantém os dados entre reinicializações. As credenciais do Compose inicializam o banco quando o volume está vazio. [Variáveis no Docker Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation)

| Variável do perfil `dev` | Valor de desenvolvimento |
| --- | --- |
| `DEV_DB_URL` | `jdbc:postgresql://127.0.0.1:5433/organizows_dev`, ajustado conforme `POSTGRES_PORT` e `POSTGRES_DB` |
| `DEV_DB_DRIVER` | `org.postgresql.Driver` |
| `DEV_DB_USER` | Mesmo valor de `POSTGRES_USER` |
| `DEV_DB_PASSWORD` | Mesmo valor de `POSTGRES_PASSWORD` |

## Roteiro para depois da revisão

Na raiz do repositório, copie o exemplo e preencha a senha local no arquivo criado.

```powershell
Copy-Item docs/scrum-91/.env.example docs/scrum-91/.env
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml config --quiet
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml up -d
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml ps
```

Com o banco disponível e as dependências revisadas, defina as quatro variáveis `DEV_DB_*` no terminal ou na configuração de execução da IDE e inicie o backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Na primeira execução com um volume novo, registre a conexão JDBC, o resultado da aplicação das migrações Flyway em base vazia e a resposta de `GET /api/v1/status`. A execução do Compose, a conexão JPA e a compatibilidade das migrações com PostgreSQL permanecem pendentes.
