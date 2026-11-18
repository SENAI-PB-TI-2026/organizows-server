---
name: jira-task-planning
description: Traduzir tarefas Jira SCRUM e seus critérios de aceite em pequenos passos de implementação compreensíveis para estudantes. Use no início de cada tarefa e ao revisar escopo.
---

# Planejamento simples a partir do Jira

1. Leia a tarefa, responsável e critérios de aceite (ou peça o texto se não puder acessar o Jira). Não invente requisitos.
2. Compare com `origin/develop` e liste o que já existe, o que precisa criar e o que depende da contribuição de outro colega.
3. Organize passos pequenos: modelo (se necessário), repository, service, controller, DTOs e verificação manual, apenas quando exigidos pelo cartão.
4. Explique brevemente cada passo em linguagem adequada a alunos.
5. Evite criar funções não pedidas e bibliotecas novas sem justificativa; preserve classes dos demais integrantes.
6. Para cada critério, proponha um exemplo manual de verificação: ação, resultado esperado, resposta recebida (somente se testada).
7. No final, apresente `critério | arquivo(s) | modo de verificar | situação`. Não marque concluído apenas por existir código.
8. Não altere o status no Jira sem autorização. Não presuma que testes automatizados são obrigatórios.
