---
name: pull-request-review
description: Revisar pull requests do Organizows contra develop, critérios Jira, regressões e evidências de testes; preparar feedback sem efetuar merge. Use em revisão cruzada, conflitos e decisão de integração.
---

# Revisão cruzada

1. Confira origem/destino do PR, commits SHA, mudanças, conflitos e se já existe implementação equivalente em `develop`. Não analise apenas o diff contra `main`.
2. Leia ticket Jira, dependências e critérios. Examine versões atuais de arquivos críticos e compare migrations Flyway.
3. Busque riscos: downgrade de Java/Spring, modelo `Local` duplicado, migração V1 repetida, API divergente, CORS wildcard, `ddl-auto=update`, senhas fixas, estoque negativo/overflow, concorrência e ausência de testes.
4. Verifique CI/workflows e logs; se indisponíveis, diferencie teste escrito de teste executado. Não invente tempos, cobertura ou aprovação.
5. Classifique achados por severidade, com arquivo, comportamento atual, risco, correção esperada e teste de regressão.
6. Oriente a correção na própria branch e proponha prompt para agente quando útil. Não faça merge, feche PR ou altere Jira sem autorização.
7. Se o PR for integralmente redundante por outro merge, documente commit que incorporou trabalho e o que mudaria caso fosse reintroduzido.

## Formato da revisão
Resumo | matriz Jira | bloqueadores | melhorias | testes evidenciados | recomendação | próximo passo.
