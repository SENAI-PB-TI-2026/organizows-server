---
name: git-pr-workflow
description: Orientar estudantes do Organizows a criar branches a partir de develop, revisar mudanças, organizar commits e abrir PRs rastreáveis. Use em tarefas de Git, push e revisão em equipe.
---

# Fluxo de Git para a equipe

1. Confira `git status` e `git branch --show-current`. Evite operar em cima de alterações locais alheias.
2. Atualize referências com `git fetch origin`. Para tarefa nova, parta de `develop`: `git switch develop`, `git pull --ff-only origin develop`, `git switch -c feat/SCRUM-N-descricao`. Se já existir branch em andamento, não a substitua.
3. Durante o desenvolvimento, faça alterações pequenas e relacionadas ao cartão. Antes de preparar commit, leia `git diff` e `git status --short`.
4. Selecione arquivos específicos com `git add caminho/do/arquivo`; não inclua `target/`, `.idea/`, `.env` nem arquivos pessoais.
5. Use a skill `commit-message` para propor a mensagem e explicar o tipo. Só faça commit e push mediante autorização do aluno.
6. Abra o PR de `feat/... -> develop`, com link Jira, descrição, arquivos afetados, como verificar manualmente e pontos incompletos. Use o template do repositório.
7. Solicite revisão cruzada. Caso surjam conflitos, explique-os antes de resolver, compare com `origin/develop` e não rebaixe versões do projeto nem descarte commits de colegas.
8. Não execute merge, rebase destrutivo, force-push, delete branch, tag ou release sem autorização.

## Exemplo de PR
- Objetivo: permitir consultar localização pelo código.
- Jira: SCRUM-19.
- Implementação: endpoint GET e DTO de resposta.
- Verificação manual: consultar código existente (200) e inexistente (404) no Swagger.
- Pendências: registrar o que não foi implementado, sem declarar "tudo funcionando" sem execução.
