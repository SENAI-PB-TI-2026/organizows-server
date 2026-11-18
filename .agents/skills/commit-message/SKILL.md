---
name: commit-message
description: Criar ou revisar mensagens de commit para tarefas SCRUM do Organizows usando Conventional Commits, escopo e descrição clara. Use antes de git commit ou ao revisar histórico.
---

# Padronização de mensagens de commit

## Formato obrigatório
```text
tipo(escopo): [SCRUM-N] descrição curta no infinitivo
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

## Lista rápida
- Tipo corresponde à mudança?
- Chave SCRUM correta?
- Escopo identifica a área afetada?
- Descrição indica o que mudou?
- Sem segredos ou arquivos de IDE no commit?

Siga a convenção oficial em `CONTRIBUTING.md` se houver diferença de estilo.
