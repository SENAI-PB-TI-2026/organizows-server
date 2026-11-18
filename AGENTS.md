# Instruções para agentes — Organizows Server

Este projeto é desenvolvido por alunos em formação. Ajude a aplicar **os conteúdos já estudados**, com explicações curtas e código compreensível. Não imponha arquitetura avançada, testes automatizados, CI/CD ou ferramentas ainda não ensinadas. Sugira melhorias futuras separadamente, sem torná-las pré-requisitos.

## Antes de programar
1. Identifique o cartão `SCRUM-N` e leia seu objetivo e critérios de aceite. Se não houver acesso ao Jira, peça o texto e informe o limite.
2. Confira a branch `develop`, `CONTRIBUTING.md`, o código existente e a tarefa em andamento antes de editar. Não sobrescreva trabalho de colegas.
3. Explique em poucas linhas o que será implementado, quais arquivos serão modificados e como verificar manualmente o resultado.
4. Faça alterações pequenas, diretamente relacionadas à tarefa; preserve a estrutura adotada pelo projeto.

## Padrões do projeto
- Java 25, Spring Boot e Maven Wrapper nas versões existentes em `develop`. Não troque versões do `pom.xml` por conveniência.
- Pacote-base `br.senai.organizows`, com `controller`, `service`, `repository`, `model` e `dto` conforme o exemplo integrado.
- Controllers recebem requisições e chamam services; services aplicam regras; repositories acessam dados. Utilize DTOs e tratamento de erros existentes em vez de repetir código.
- A localização canônica chama-se `Localizacao`. Consulte implementações existentes antes de adicionar outra entidade semelhante.
- Não coloque senhas, chaves ou dados reais de pessoas nos commits. Não altere migrações de banco já existentes/aplicadas.
- Escreva nomes claros em português ou conforme o padrão das classes existentes; evite abreviações sem sentido e comentários óbvios.

## Git e entrega
- Use branch criada a partir de `develop`, contendo `SCRUM-N`; não faça commit direto em `main` ou `develop`.
- Commits devem usar `tipo(escopo): [SCRUM-N] descrição`. Veja a skill `commit-message`.
- Antes do commit: revise `git status`, `git diff` e `git diff --cached`; inclua apenas arquivos relacionados à tarefa.
- Verifique o resultado de forma **manual** conforme o que foi ensinado: executar a aplicação quando possível, testar a requisição no Swagger/Postman e observar mensagens de erro. Relate o que foi ou não executado.
- Abra PR para `develop` com link Jira, resumo, passos de verificação e pendências. Revisão por outro integrante é obrigatória segundo `CONTRIBUTING.md`.
- Não crie testes automatizados, frameworks de testes, pipelines ou novas dependências sem necessidade expressa no cartão ou orientação do professor.
- Não faça merge, force-push, mudança no Jira ou release por conta própria.

## Skills compartilhadas
- `commit-message`: sugerir e validar mensagens de commit.
- `java-spring-basics`: escrever código legível e organizado conforme o projeto.
- `git-pr-workflow`: orientar branches, alterações, PR e revisão.
- `jira-task-planning`: transformar os critérios Jira em passos pequenos.

Se uma instrução entrar em conflito com o código vigente, explique a divergência antes de editar. Nunca afirme que executou comandos ou verificações que não executou.
