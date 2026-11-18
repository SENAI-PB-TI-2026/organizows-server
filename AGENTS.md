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

## Seleção automática das skills (sem exigir invocação no pedido)
As regras deste `AGENTS.md` aplicam-se em **todas as tarefas**. Os alunos não precisam escrever nomes de skills nem comandos especiais. Antes de responder ou executar uma solicitação de desenvolvimento, identifique o tipo de atividade e **consulte automaticamente a skill correspondente** em `.agents/skills/<nome>/SKILL.md`, se o agente tiver acesso a arquivos. Leia a skill completa quando ela for pertinente; não carregue as quatro sem necessidade.

- **Mensagem de commit, preparação ou revisão de commit** → `.agents/skills/commit-message/SKILL.md`.
- **Criar ou editar classes Java/Spring, implementar endpoint, corrigir lógica ou organizar código** → `.agents/skills/java-spring-basics/SKILL.md`.
- **Git, branches, stage, commits, push, Pull Requests e conflitos** → `.agents/skills/git-pr-workflow/SKILL.md`; combine com `commit-message` quando houver proposta de commit.
- **Cartão Jira SCRUM-N, planejamento, critérios de aceite ou início de tarefa** → `.agents/skills/jira-task-planning/SKILL.md`; combine com `java-spring-basics` se a tarefa envolver código.

Na dúvida, aplique as regras gerais deste arquivo e consulte apenas as skills relacionadas. Se uma ferramenta não descobrir skills automaticamente, tente ler o caminho indicado; se não puder, continue com as regras gerais e informe essa limitação sem fingir ter lido a skill. Nunca afirme que todas as skills foram aplicadas se não foram consultadas. A seleção de skills continua sujeita ao suporte do agente; **não é possível garantir leitura integral a cada mensagem por simples instrução**.

Se uma instrução entrar em conflito com o código vigente, explique a divergência antes de editar. Nunca afirme que executou comandos ou verificações que não executou.

## Limites do conteúdo ministrado: autenticação e autorização
- **Autenticação e autorização ainda não foram ensinadas às equipes.** Não introduza nem configure Spring Security, JWT, OAuth2, tokens, login, senhas, roles, permissões, `@PreAuthorize` ou sistemas de usuários por iniciativa própria.
- Cartões existentes sobre autenticação/autorização (ex.: SCRUM-14, SCRUM-24, SCRUM-25 e tarefas relacionadas) são **escopo futuro dependente de orientação do professor**. Não marque como concluídos, não implemente atalhos e não altere o Jira automaticamente.
- Se o aluno pedir uma dessas funcionalidades, explique que ela exige conteúdo ainda não ministrado, identifique a dependência e solicite orientação do professor antes de implementar. A instrução explícita do professor prevalece quando esse conteúdo for introduzido.
- **Não confunda ausência de autenticação no protótipo com segurança de produção.** Não afirme que endpoints de escrita públicos são adequados para implantação real; não publique serviços acessíveis externamente sem autorização e avaliação de segurança.
- Continue validando entradas, usando tratamento de exceções existente e evitando credenciais no código; essas práticas não dependem de login.

## Início de tarefa com descrição copiada do Jira (procedimento obrigatório)
O aluno normalmente **não conecta o agente ao Jira**: ele abre a `develop` localmente, copia a **descrição completa do cartão Jira** (incluindo o bloco "Instruções para o agente") e envia o texto ao agente. O agente deve extrair a chave `SCRUM-N`, o objetivo, o responsável, os critérios e o nome sugerido de branch diretamente do texto. **Não condicione a execução à conexão Jira.** Se houver chave, critérios e escopo suficientes no texto, prossiga sem perguntar sobre credenciais do Jira.
1. **Antes de escrever qualquer código**, rode `git status --short`, `git branch --show-current` e `git fetch origin`. Se há alterações não commitadas, pare antes de trocar de branch e oriente o aluno a preservá-las (commit em branch própria ou stash autorizado); não descarte nem mova alterações inadvertidamente.
2. Confirme `develop` como base limpa. Se o aluno iniciou na `develop` limpa, use `git switch develop` e `git pull --ff-only origin develop`; se isso não for possível, apresente o problema. Não faça rebase ou reset forçado.
3. Leia `Branch sugerida` no texto colado e confira que segue `<tipo>/SCRUM-N-<slug>`. A branch deve representar a **tarefa inteira**, não o nome do aluno. Se o bloco não tiver branch, derive o tipo e slug do título/objetivo, em minúsculas, sem acentos, espaços ou caracteres especiais. Prefixos: `feature/` (nova funcionalidade, conforme material de aula), `fix/`, `docs/`, `chore/`; o alias `feat/` existente pode ser aceito para branches antigas, mas novas branches devem priorizar `feature/`.
4. Confira se a branch local/remota já existe. **Não crie duplicata**, não sobrescreva outra branch e não use a `main` como origem. Crie e mude para a nova branch com `git switch -c <branch>`, partindo da `develop` atualizada. Criar a branch local faz parte da solicitação de implementação; **commit, push, PR e merge continuam sujeitos à autorização expressa do aluno**.
5. Apresente a chave, a branch criada e um resumo dos critérios antes de implementar; aplique `jira-task-planning`, `java-spring-basics` e `commit-message` quando pertinentes.
6. Se o cartão está em outra sprint, marcado concluído, destinado a outro aluno ou ligado a autenticação/autorização ainda não ministrada, não execute automaticamente: explique o conflito e solicite validação do professor. Não invente status, sprint ou autorização ausentes.
7. A mensagem de commit deriva do trabalho **realmente feito**, não do título isolado: `tipo(escopo): [SCRUM-N] verbo no imperativo + objeto`. Sugira a mensagem, revise o diff e peça autorização antes de executar o commit.
