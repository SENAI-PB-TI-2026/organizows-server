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

## Checagem de escopo pedagógico antes de abrir PR
Verifique se o diff introduziu autenticação/autorização não solicitada ou requisitos ainda não ministrados. Se sim, pare e explique a necessidade de alinhamento com o professor antes de incluir tais mudanças. Não declare segurança para uso público sem verificação adequada.

## Fluxo automático para descrição colada do Jira
O ponto de partida esperado é uma `develop` local **limpa**. Ao receber a descrição da tarefa, identifique o bloco "Instruções para o agente" e, **antes de implementar**, extraia `Chave`, `Branch sugerida`, `Tipo` e `Objetivo`. Não exija acesso à API Jira. Confirme a situação atual com `git status --short`, `git branch --show-current` e `git fetch origin`; atualize `develop` com `git pull --ff-only origin develop`; verifique a inexistência da branch local/remota; então crie com `git switch -c feature/SCRUM-N-descricao-curta` (ou o prefixo indicado). Nunca faça essa troca com alterações locais não preservadas. O aluno não precisa pedir separadamente a criação da branch: ela faz parte do início do trabalho; só commit, push, PR e merge requerem autorização explícita.

**Padrão inspirado na aula:** `feature/` para funcionalidades novas, `fix/` para correções, `docs/` para documentação, `chore/` para manutenção. Acrescente sempre `SCRUM-N` e um slug curto. Exemplo: `feature/SCRUM-22-persistencia-materiais`. O branch `feat/` é legado aceito quando já existe e não deve ser recriado apenas para renomear.

Quando houver alterações locais, oriente opções seguras: salvar em branch própria ou `git stash push -m "trabalho antes de SCRUM-N"` com autorização; ao retomar, `git stash list` e `git stash pop` com cuidado. Nunca descarte alterações usando `git reset --hard` ou `git clean -fd`. Não faça merge local para `develop` sem PR.
