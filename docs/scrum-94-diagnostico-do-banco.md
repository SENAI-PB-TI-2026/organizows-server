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

A rodada local foi feita sobre a `develop` no commit `1ff1646`, combinando em uma cópia de validação as dependências do PR #19 e o diagnóstico do PR #21. O PostgreSQL 14 rodou em uma instância isolada. A repetição na branch atualizada depois do merge do #19 permanece pendente.

```powershell
.\mvnw.cmd -B -DskipTests package
java -jar target/organizows-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --spring.main.web-application-type=none --debug --logging.level.org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter=DEBUG --logging.level.com.zaxxer.hikari=DEBUG
```

Repita o comando com uma senha incorreta em `DEV_DB_PASSWORD` e com uma porta sem servidor em `DEV_DB_URL`, um cenário por vez. Para reproduzir a URL vazia, acrescente `--spring.datasource.url=`. Para verificar o fallback, remova do ambiente de teste o `.env` e as variáveis de conexão. Para conferir o isolamento, use `--spring.profiles.active=h2` com `DEV_DB_URL` inválida.

- **PostgreSQL válido:** o Flyway aplicou V1 a V4 em banco vazio, o Hibernate inicializou com validação do esquema e a aplicação iniciou.
- **Diagnóstico do projeto, senha incorreta:** “O banco de desenvolvimento recusou a autenticação.” A orientação indica `DEV_DB_USER` e `DEV_DB_PASSWORD`.
- **Diagnóstico do projeto, servidor indisponível:** “Falha ao estabelecer conexão com o banco de desenvolvimento.” A orientação indica revisar host, porta e banco em `DEV_DB_URL`.
- **Diagnóstico do projeto, URL vazia:** “A URL JDBC do perfil dev está vazia.” A aplicação encerrou com código 1.
- **Diagnósticos nativos:** o Spring Boot informou `Cannot load driver class: exemplo.Driver` e o Hikari informou `claims to not accept jdbcUrl` no cenário de URL incompatível.
- **H2:** o fallback `dev` iniciou em `organizows_dev` e o perfil `h2` isolado iniciou em `organizows`, mesmo com `DEV_DB_URL` inválida.

As falhas de PostgreSQL foram executadas com marcadores fictícios na senha e, separadamente, em um parâmetro de URL. Os marcadores não apareceram na saída capturada. O diagnóstico próprio usa uma causa com mensagem fixa para o relatório em DEBUG. Os diagnósticos nativos seguem habilitados.
