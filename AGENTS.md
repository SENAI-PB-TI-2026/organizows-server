# Orientações permanentes para agentes — Organizows Server

Leia este arquivo antes de modificar qualquer código. A referência de integração é `origin/develop`; `main` recebe releases. As decisões já integradas têm prioridade sobre alterações de branches antigas.

## Contrato do projeto
- Backend Java 25, Spring Boot 4.1.2, Maven Wrapper, JPA, Flyway, H2 para desenvolvimento/teste e PostgreSQL somente após configuração explícita. Confira sempre o `pom.xml` e o código atual; não rebaixe versões para "resolver" erros.
- Código em `br.senai.organizows`; separe controller, service, repository, model e DTO. APIs usam `/api/v1/`, `ApiResponse`, `ApiErrorResponse` e `GlobalExceptionHandler`. Reuse `CorsConfig`.
- `Localizacao` é o modelo hierárquico integrado; não crie `Local` paralelo. Coordene `Material` e `Peca` com os responsáveis antes de duplicar cadastros.
- Banco: migrations Flyway incrementais e imutáveis após aplicadas; `spring.jpa.hibernate.ddl-auto=validate`; nunca `update` como atalho nem duas migrations com a mesma versão.
- Não registre credenciais, tokens, dados pessoais reais ou segredos no repositório. Nunca habilite CORS universal por comodidade.
- Siga `CONTRIBUTING.md`: branch criada de `develop`, ticket Jira `SCRUM-N`, commits convencionais com `[SCRUM-N]`, PR revisado por outra pessoa e evidências de teste.

## Protocolo antes de editar
1. Identifique ticket e leia todos os critérios de aceite. Se o Jira estiver inacessível, solicite/registre os critérios e não assuma sua aprovação.
2. Inspecione `git status`, `origin/develop`, arquivos relacionados, migrations, DTOs e testes; determine o menor diff possível.
3. Produza plano breve, implemente por camadas e teste cenários positivos, negativos e integrações.
4. Execute `./mvnw clean verify` com JDK 25 quando disponível. Não declare testes aprovados sem execução. Se bloqueado, informe o motivo.
5. Revise diff e faça relatório com critérios atendidos, arquivos, comandos executados, resultados reais, riscos e pendências.
6. Não faça merge, force-push, release, publicação, mudança no Jira ou alteração em outras branches sem autorização explícita.

## Skills disponíveis (carregue apenas a relevante)
- `jira-task-delivery`: tarefa Jira até evidência de aceite.
- `spring-api-patterns`: APIs, DTOs, validações e HTTP.
- `flyway-data-integrity`: modelagem, migrations e consistência.
- `inventory-concurrency`: saldos, movimentações e concorrência.
- `test-and-verify`: testes, build, evidência e pré-produção.
- `pull-request-review`: revisão, integração e rastreabilidade.
