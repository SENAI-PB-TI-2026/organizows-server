# SCRUM-94 · Diagnóstico do banco no perfil dev

[Cartão no Jira](https://team-senai.atlassian.net/browse/SCRUM-94)

As variáveis externas `DEV_DB_*` têm precedência sobre o `.env` da raiz. Sem configuração externa, o perfil `dev` usa H2 em memória com senha vazia. Ao usar PostgreSQL, configure a URL, o driver, o usuário e a senha em campos separados. Mantenha credenciais fora da URL JDBC e do Git.

Inicie o backend com `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"` na raiz. Para verificar cenários inválidos, altere temporariamente a configuração no terminal ou na IDE e compare a saída de inicialização com os resultados abaixo.

| Cenário | Resultado observado | Como corrigir |
| --- | --- | --- |
| Perfil `dev` sem configuração externa nem `.env` | H2 iniciou com senha vazia, Flyway aplicou V1 a V4 e o JPA inicializou. | Use o fallback para desenvolvimento local. |
| URL JDBC definida como vazia | A aplicação encerrou com diagnóstico que identifica `DEV_DB_URL` e `spring.datasource.url`. | Preencha a URL ou remova a definição vazia para usar H2. |
| URL incompatível com o driver | O Hikari rejeitou a URL JDBC. | Confira se `DEV_DB_URL` e `DEV_DB_DRIVER` apontam para o mesmo tipo de banco. |
| Classe de driver inexistente | O Spring Boot identificou a classe de driver que não conseguiu carregar. | Corrija `DEV_DB_DRIVER` e confirme que o driver está no classpath. |
| PostgreSQL com senha incorreta, usando as dependências da SCRUM-91 | A aplicação encerrou com orientação para conferir usuário e senha. | Revise `DEV_DB_USER` e `DEV_DB_PASSWORD`. |
| PostgreSQL indisponível, usando as dependências da SCRUM-91 | A aplicação encerrou com orientação para conferir o servidor e a URL. | Confira se o PostgreSQL está ativo e se host, porta e banco estão corretos. |
| PostgreSQL válido, usando as dependências da SCRUM-91 | Conexão estabelecida, esquema validado e inicialização concluída. | Use as quatro variáveis do perfil `dev`. |
| Variáveis externas de URL, driver e usuário com valores diferentes do `.env` | A aplicação conectou ao banco definido no ambiente externo. | Confira as variáveis da sessão e as opções de execução da IDE. |
| Perfil `h2` com `DEV_DB_URL` inválida | O perfil `h2` iniciou com a própria configuração. | Use `DEV_DB_*` para configurar somente o perfil `dev`. |

## Verificação com PostgreSQL e DEBUG

A primeira rodada usou PostgreSQL 14 isolado e uma cópia da `develop` no commit `1ff1646`. Em 26/11/2026, repetimos a validação em uma cópia isolada da `origin/develop` no commit `d5341dd`, com as dependências JDBC/Flyway do [PR #19](https://github.com/SENAI-PB-TI-2026/organizows-server/pull/19) e o diagnóstico do [PR #21](https://github.com/SENAI-PB-TI-2026/organizows-server/pull/21). O PostgreSQL 17.12 subiu saudável com o Compose da SCRUM-91, em um volume novo na porta local 5434. Na data da rodada, a integração do #19 à `develop` ainda estava pendente.

```powershell
.\mvnw.cmd -B -DskipTests package
java -jar target/organizows-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --spring.main.web-application-type=none --debug --logging.level.org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter=DEBUG --logging.level.com.zaxxer.hikari=DEBUG
```

Foram usadas as quatro variáveis `DEV_DB_*` com o PostgreSQL ativo. Para testar autenticação, foi definida uma senha fictícia incorreta em `DEV_DB_PASSWORD`. Para testar indisponibilidade, o contêiner de validação foi parado e a URL recebeu um parâmetro `password` com outro marcador fictício. Para reproduzir a URL vazia, acrescente `--spring.datasource.url=`. Para verificar o fallback, execute sem `.env` e sem as variáveis de conexão. Para conferir o isolamento, use `--spring.profiles.active=h2` com `DEV_DB_URL` inválida.

- **PostgreSQL válido:** o Hikari abriu uma conexão JDBC, o Flyway aplicou V1 a V4 em banco vazio, o Hibernate inicializou com validação do esquema e a aplicação encerrou com código 0 no modo sem servidor web.
- **Diagnóstico do projeto, senha incorreta:** código 1 e mensagem “O banco de desenvolvimento recusou a autenticação.” A orientação foi “Confira DEV_DB_USER e DEV_DB_PASSWORD no ambiente e no .env da raiz.” Em DEBUG, a causa exibida foi “Falha de autenticação JDBC no perfil dev.”
- **Diagnóstico do projeto, servidor indisponível:** código 1 e mensagem “Falha ao estabelecer conexão com o banco de desenvolvimento.” A orientação foi “Confira se o banco está ativo e se host, porta e banco em DEV_DB_URL estão corretos.” Em DEBUG, a causa exibida foi “Falha de conexão JDBC no perfil dev.”
- **Diagnóstico do projeto, URL vazia:** “A URL JDBC do perfil dev está vazia.” A aplicação encerrou com código 1.
- **Diagnósticos nativos:** o Spring Boot informou `Cannot load driver class: exemplo.Driver` e o Hikari informou `Driver org.h2.Driver claims to not accept jdbcUrl` no cenário de URL incompatível. Ambos encerraram com código 1.
- **H2:** o fallback `dev` iniciou em `organizows_dev` sem senha configurada. O perfil `h2` iniciou em `organizows` mesmo com `DEV_DB_URL` apontando para o PostgreSQL parado. Ambos aplicaram V1 a V4 e encerraram com código 0.
- **Precedência:** com URL, driver e usuário diferentes no `.env` e no ambiente externo, a aplicação iniciou no H2 `scrum94_externo` definido pelo ambiente.

A saída completa capturada nos cenários não continha a senha local gerada nem os marcadores fictícios de senha e URL. O diagnóstico próprio usa uma causa com mensagem fixa para o relatório em DEBUG. Os diagnósticos nativos seguem habilitados. Mantenha a senha em `DEV_DB_PASSWORD` para uso normal.
