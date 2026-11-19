# SCRUM-77 — Verificação manual de persistência

Use o JDK 25, PowerShell e o Maven Wrapper da raiz do repositório. Confira a instalação com `java -version` e `.\mvnw.cmd -v`. Este roteiro começa na inicialização da aplicação. Para preparação do ambiente e compilação, consulte a seção **Como Executar o Projeto** do [README](../README.md) e o guia da SCRUM-83 quando estiver disponível.

## Iniciar com H2 em memória

Use um terminal local sem sobrescritas de datasource ou Flyway. Os comandos abaixo usam a porta 8080.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'h2'
.\mvnw.cmd spring-boot:run
```

Procure nos logs a conexão com `jdbc:h2:mem:organizows`, a aplicação da migração `1 - create table localizacao` e a mensagem `Started OrganizowsApplication`. Para usar o perfil `dev`, encerre a aplicação com Ctrl+C e repita o comando com `$env:SPRING_PROFILES_ACTIVE = 'dev'`. Nesse perfil, a URL padrão é `jdbc:h2:mem:organizows_dev`.

Em outro terminal, consulte o endpoint existente. Em um banco novo, a resposta é HTTP 200 com `data: []`.

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/v1/localizacoes |
    Select-Object StatusCode, Content
```

## Conferir as tabelas e o histórico de migrações

Encerre a execução anterior. O H2 em memória pertence ao processo da aplicação. Para consultar o banco por outro processo depois de desligá-la, inicie o perfil `dev` com um novo arquivo temporário.

```powershell
$pasta = Join-Path $env:TEMP ('organizows-scrum77-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $pasta | Out-Null
$url = 'jdbc:h2:file:' + ($pasta -replace '\\', '/') + '/persistencia'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DEV_DB_URL = $url
.\mvnw.cmd spring-boot:run
```

Confirme a aplicação da V1 e encerre com Ctrl+C para liberar o arquivo. Se repetir a inicialização com a mesma URL, o Flyway deve informar que o esquema está atualizado. Encerre novamente antes de abrir o H2 Shell. No mesmo terminal, use o JAR do H2 resolvido pelo Maven Wrapper nesta versão da base. Se o Maven usar um repositório local personalizado, ajuste o caminho do JAR.

```powershell
$h2Jar = Join-Path $env:USERPROFILE '.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar'
java -cp $h2Jar org.h2.tools.Shell -url "$url;IFEXISTS=TRUE" -user sa -password '""'
```

No prompt `sql>`, execute as consultas abaixo. A lista deve conter `LOCALIZACAO` e `flyway_schema_history`. A V1 deve aparecer com `success = TRUE`. O Flyway também pode registrar uma linha interna com versão nula.

```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME;
SELECT COUNT(*) AS TOTAL FROM PUBLIC.LOCALIZACAO;
SELECT "version", "script", "success" FROM PUBLIC."flyway_schema_history"
ORDER BY "installed_rank";
```

Saia do Shell com `exit` e limpe a sobrescrita com `Remove-Item Env:DEV_DB_URL -ErrorAction SilentlyContinue`.

## Reproduzir falhas comuns

Execute cada cenário com a aplicação parada e o perfil `dev` ativo. As alterações abaixo valem para o terminal e usam apenas bancos descartáveis.

- **Flyway desativado em um banco vazio.** Remova a variável `DEV_DB_URL` para voltar ao banco em memória. Defina `$env:SPRING_FLYWAY_ENABLED = 'false'` e execute `.\mvnw.cmd spring-boot:run`. A inicialização falha com `Schema validation: missing table [localizacao]` porque o Hibernate valida a tabela que o Flyway criaria. Remova `SPRING_FLYWAY_ENABLED` do ambiente e execute novamente.
- **URL aponta para um banco inexistente e bloqueia sua criação.** Defina `$env:DEV_DB_URL = 'jdbc:h2:mem:scrum77_ausente;IFEXISTS=TRUE'` e execute `.\mvnw.cmd spring-boot:run`. A conexão falha com `Database "mem:scrum77_ausente" not found` (`90146-240`). Remova `DEV_DB_URL` do ambiente para voltar ao padrão.

Use `Remove-Item Env:NOME_DA_VARIAVEL -ErrorAction SilentlyContinue` para desfazer as sobrescritas acima. Se o Flyway relatar divergência no histórico de um banco já migrado, registre a URL sem credenciais, a versão e a mensagem completa. Confira o histórico e o script versionado antes de decidir como corrigir o banco. Preserve as migrações já aplicadas.

## Evidências de 19/11/2026

Ambiente local com Windows, JDK 25.0.2 e H2 2.4.240. A referência `origin/develop` (`0e870cb`) foi compilada com o Maven Wrapper em uma cópia isolada. A aplicação foi iniciada pela JVM e as consultas foram executadas no H2 Shell.

| Cenário | Resultado observado |
| --- | --- |
| `origin/develop`, perfil `h2` | Conectou a `jdbc:h2:mem:organizows`, aplicou a V1 e respondeu HTTP 200 com lista vazia. |
| Branch SCRUM-77 (`7297950`), perfil `dev` | Conectou a `jdbc:h2:mem:organizows_dev`, aplicou a V1 e respondeu HTTP 200 com lista vazia. |
| `origin/develop`, perfil `dev` com H2 em arquivo | Aplicou a V1 e respondeu HTTP 200. O SQL retornou `LOCALIZACAO`, `flyway_schema_history`, zero localizações e `V1__create_table_localizacao.sql` com sucesso verdadeiro. No reinício, o Flyway informou `Schema "PUBLIC" is up to date. No migration necessary.` |
| Flyway desativado | Falha de inicialização com `Schema validation: missing table [localizacao]`. O comando documentado `spring-boot:run` também reproduziu a falha, com código de saída 1. |
| Banco inexistente com `IFEXISTS=TRUE` | Falha de conexão com `Database "mem:scrum77_ausente" not found` (`90146-240`). |

O Flyway avisou que o H2 2.4.240 é mais recente do que a versão que ele verificou (2.3.232). O aviso apareceu nas inicializações bem-sucedidas. A inspeção SQL em processo separado exigiu H2 em arquivo e a aplicação parada.
