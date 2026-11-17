---
name: test-and-verify
description: Verificar alteração Java/Maven com testes, Flyway, endpoint de saúde e preparação pré-produção. Use antes de pedir revisão, integrar ou diagnosticar falhas de build.
---

# Validação baseada em evidências

1. Verifique versão real do JDK (`java -version`) e Maven Wrapper (`./mvnw -version`); o projeto exige Java 25.
2. Rode `./mvnw --batch-mode clean verify` (Windows: `mvnw.cmd`); anote resultado e artefatos de `target/surefire-reports`. Se não executou, escreva **NÃO EXECUTADO**.
3. Teste unidade (regras), controller (HTTP/DTOs), repository (JPA), migração em banco vazio e integração transacional. Inclua erros e limites.
4. Para simular inicialização: crie JAR com Maven, inicie em perfil H2 e confira `GET /api/v1/status` e endpoints relevantes. Não confunda a resposta de status com prova de prontidão integral.
5. Para pré-produção, use banco isolado da mesma família do destino (PostgreSQL se for o alvo), credenciais efêmeras, migrations e testes funcionais. Jamais use produção como ambiente de teste.
6. Evite falsear evidências: cite comando, JDK, perfil, commit, resultado de testes, logs e falhas pendentes.
7. Sem pipeline/branch protection, aprovação local de testes não garante que todos os PRs passarão.

## Relatório de saída
`Commit | Ambiente | Comandos | Resultado por etapa | Evidência | Riscos | Veredito: aprovado / bloqueado / não executado`.
