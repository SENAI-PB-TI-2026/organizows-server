---
name: inventory-concurrency
description: Implementar ou revisar cadastro de materiais, entradas, saídas, consumo, estoque mínimo e concorrência transacional. Use em tarefas de saldo, reposição e histórico de movimentações.
---

# Regras de estoque e domínio

- Antes de criar `Peca`, `Material`, grupos ou `Local`, examine o modelo existente e o PR correspondente; trate divergências com a equipe para não duplicar cadastros.
- Separe atualização de dados cadastrais de operações de estoque; PUT cadastral não pode zerar estoque acidentalmente.
- Entrada/saída: quantidade explícita e positiva; tipo define sinal. Não aceite zero/nulo e nunca deixe saldo final negativo.
- Proteja overflow com `Math.addExact` ou aritmética ampliada + verificação de limites.
- Evite atualizações perdidas com `@Version`, lock pessimista dentro de `@Transactional` ou SQL condicional atômico; teste concorrência real. Não confunda `@Transactional` isolado com proteção suficiente.
- Atualização de saldo e registro de movimentação devem compartilhar uma transação quando histórico fizer parte do requisito; falhas devem causar rollback.
- Restrições SQL de saldo e FK são defesa complementar, não substituto da validação de domínio.
- Alerta de reposição deve respeitar regra documentada para valor igual ao mínimo; teste fronteiras e reposição.
- Não introduza autenticação inexistente como pressuposto: identifique dependência e bloqueie endpoints sensíveis quando a política exigir.

## Testes essenciais
Saldo 100-10=90; retirada excessiva sem alteração; zero/nulo; overflow; concorrência; rollback do histórico; mínimo/igual/acima; integridade SQL.
