# SCRUM-91 · Proposta de PostgreSQL para desenvolvimento local

[Cartão no Jira](https://team-senai.atlassian.net/browse/SCRUM-91)

Proposta para revisão técnica antes da aplicação. A demonstração em execução está prevista para depois da orientação docente na oficina de 04/12/2026.

## Pré-requisitos

- Java compatível com o projeto e um PostgreSQL local. Para experimentar a proposta de contêiner após a orientação docente, use Docker com o plugin Compose v2.
- Porta local 5433 disponível. O Compose publica nela a porta 5432 do contêiner, somente em `127.0.0.1`. Altere `POSTGRES_PORT` no arquivo local se necessário.
- O backend usa o driver JDBC PostgreSQL e o módulo Flyway para PostgreSQL, com versões gerenciadas pelo Spring Boot. [Suporte PostgreSQL no Flyway](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database)

## Configuração local

O Compose recebe `POSTGRES_DB` e `POSTGRES_USER` com valores fictícios de desenvolvimento. Preencha `POSTGRES_PASSWORD` apenas no `.env` local, ignorado pelo Git. O volume nomeado `postgres-data` mantém os dados entre reinicializações. As credenciais do Compose inicializam o banco quando o volume está vazio. [Variáveis no Docker Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation)

O Spring Boot lê as variáveis abaixo do `.env` na **raiz do repositório**, ou do ambiente externo, que tem precedência. Sem essas definições, o perfil `dev` usa H2 em memória. O arquivo `docs/scrum-91/.env` é exclusivo do Compose. Mantenha as senhas nos arquivos locais ignorados pelo Git ou no ambiente da IDE.

| Variável do perfil `dev` | Valor de desenvolvimento |
| --- | --- |
| `DEV_DB_URL` | `jdbc:postgresql://127.0.0.1:5433/organizows_dev`, ajustado conforme `POSTGRES_PORT` e `POSTGRES_DB` |
| `DEV_DB_DRIVER` | `org.postgresql.Driver` |
| `DEV_DB_USER` | Usuário do PostgreSQL local, por exemplo `organizows_dev`. Com Compose, use o valor de `POSTGRES_USER`. |
| `DEV_DB_PASSWORD` | Senha do usuário local. Com Compose, use o valor de `POSTGRES_PASSWORD`. |

Para uma instalação local, ajuste host, porta e nome do banco na URL JDBC. Comece com um banco vazio para verificar a aplicação das migrações.

## Roteiro para depois da revisão

Na raiz do repositório, copie o exemplo e preencha a senha local no arquivo criado.

```powershell
Copy-Item docs/scrum-91/.env.example docs/scrum-91/.env
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml config --quiet
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml up -d
docker compose --env-file docs/scrum-91/.env -f docs/scrum-91/compose.yaml ps
```

Com o banco disponível, preencha as quatro variáveis `DEV_DB_*` no `.env` da raiz ou no ambiente externo e inicie o backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Na primeira execução com um volume novo, registre a conexão JDBC, o resultado das migrações Flyway em base vazia e a inicialização do Hibernate. Consulte `GET /api/v1/status` para confirmar que o serviço está no perfil `dev`.

## Verificação manual do hotfix

Em uma cópia da `develop` com as dependências deste hotfix, executei o perfil `dev` com PostgreSQL 14 local em uma instância isolada e um banco vazio. O Hikari abriu uma conexão JDBC, o Flyway aplicou V1 a V4, o Hibernate validou o esquema e `GET /api/v1/status` retornou HTTP 200 com `environment: dev`. Também iniciei o perfil `dev` sem `.env` e confirmei o fallback H2, as quatro migrações e a inicialização JPA. A demonstração em Docker Compose aguarda a orientação docente.
