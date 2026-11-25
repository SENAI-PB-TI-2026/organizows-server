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

A orientação adicional de conexão e autenticação exibe apenas os nomes das variáveis. O Spring Boot e o Hikari continuam responsáveis pelos diagnósticos de driver e compatibilidade de URL.
