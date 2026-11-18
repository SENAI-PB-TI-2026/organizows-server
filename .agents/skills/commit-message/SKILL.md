---
name: commit-message
description: Criar ou revisar mensagens de commit para tarefas SCRUM do Organizows usando Conventional Commits, escopo e descrição clara. Use antes de git commit ou ao revisar histórico.
---

# Padronização de mensagens de commit

## Formato obrigatório
```text
tipo(escopo): [SCRUM-N] descrição curta com verbo no imperativo
```

Exemplos:
- `feat(localizacao): [SCRUM-19] adicionar consulta por codigo`
- `fix(materiais): [SCRUM-22] impedir quantidade negativa`
- `docs(readme): [SCRUM-28] explicar fluxo de contribuicao`
- `refactor(service): [SCRUM-6] separar validacao do cadastro`
- `chore(config): [SCRUM-76] organizar configuracoes de ambiente`

Tipos: `feat` (funcionalidade), `fix` (correção), `docs` (documentação), `refactor` (organização sem mudança de comportamento), `style` (formatação), `chore` (manutenção), `build` (build/dependências). Outros tipos de `CONTRIBUTING.md` só quando apropriados à tarefa.

## Passos do agente
1. Leia `git status --short`, `git diff --cached` (ou as alterações fornecidas). Não invente alterações.
2. Identifique qual SCRUM-N está sendo atendida; se desconhecida, não invente chave.
3. Se houver mudanças de objetivos distintos, **sugira commits separados** e explique quais arquivos vão em cada um.
4. Proponha **uma mensagem específica** para as alterações realmente preparadas; não use "ajustes", "final teste", "implementando" nem mensagens genéricas.
5. Nunca execute `git add .`, `git commit`, push ou force-push sem autorização; se autorizado, inclua somente arquivos pertinentes e mostre mensagem final.

## Padrão ensinado na aula
Em mensagens de commit use verbo de ação no imperativo (ex.: `cria`, `corrige`, `adiciona`, `atualiza`), nunca gerúndio ou particípio. A rastreabilidade da turma acrescenta `[SCRUM-N]` ao Conventional Commit, entre os dois pontos e a descrição.

A mensagem de commit **não deve ser copiada mecanicamente** de `Commit sugerido` no Jira: valide o que foi realmente alterado. Se `feat` no bloco da tarefa virou correção de bug, use `fix`; registre o mesmo `SCRUM-N`.
Se o aluno digitou a mensagem errada num commit **ainda não enviado**, explique `git commit --amend -m "mensagem corrigida"`, com autorização. Em commits já enviados, não reescreva o histórico compartilhado.
Em branches, use `feature/` para nova funcionalidade (conforme material da aula), enquanto o tipo de commit equivalente é `feat`.

## Lista rápida
- Tipo corresponde à mudança?
- Chave SCRUM correta?
- Escopo identifica a área afetada?
- Descrição indica o que mudou?
- Sem segredos ou arquivos de IDE no commit?

Siga a convenção oficial em `CONTRIBUTING.md` se houver diferença de estilo.

Não use mensagens de commit para justificar adição de autenticação/autorização fora do escopo ensinado. Antes de sugerir um commit, indique se o diff introduz login, JWT, permissões ou usuários sem orientação docente.
