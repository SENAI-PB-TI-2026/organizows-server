---
name: jira-task-delivery
description: Implementar uma tarefa SCRUM do Organizows do entendimento dos critérios de aceite até a entrega rastreável em PR, sem ampliar o escopo. Use quando receber uma issue Jira, iniciar uma feature ou preparar a entrega.
---

# Entrega orientada ao Jira

1. Leia descrição, critérios, dependências, sprint e responsável do cartão via Jira ou texto fornecido. Se não tiver acesso, marque "não verificado" e peça dados necessários.
2. Compare cada critério com o estado real de `origin/develop`. Localize código reutilizável e PRs relacionados.
3. Registre uma matriz `critério | implementação | teste | evidência | status`; não conclua critério apenas por existir uma classe.
4. Planeje mudança mínima. Trabalhe em branch de `develop` com `SCRUM-N`; não implemente escopo de outro responsável silenciosamente.
5. Use as skills de API, banco, estoque e testes conforme o tipo da tarefa.
6. Antes de entregar: execute testes; inspecione diff; atualize PR com link da issue, decisões, riscos e evidência factual. Commits seguem `CONTRIBUTING.md`.
7. Se houver critério não verificado, mantenha-o pendente. Não mude o status Jira nem faça merge sem autorização.

## Resultado obrigatório
Matriz de critérios e evidências; arquivos alterados; testes realmente executados; bloqueios; pedido de revisão cruzada.
