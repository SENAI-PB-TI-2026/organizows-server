# Skills compartilhadas para os alunos — Organizows

Estas instruções são versionadas no projeto e podem ser reutilizadas por agentes que suportam skills de repositório. Elas **não substituem o conteúdo de aula, o código que já existe nem a revisão dos colegas**.

## O que é esperado nesta etapa do curso
- Mensagens de commit com padrão e número do cartão SCRUM.
- Código Java/Spring Boot claro e organizado.
- Mudanças pequenas, em branch própria, integradas via PR para `develop`.
- Descrição do que foi feito e **verificação manual** no Swagger/Postman quando pertinente.
- Não exigir testes automatizados ou CI; esses assuntos serão introduzidos em momento oportuno.

## Como usar
1. Após o merge deste PR, atualize sua branch com as mudanças de `develop`.
2. Abra a raiz do projeto no editor e reinicie a sessão do agente, se necessário.
3. Peça a utilização da skill necessária ou solicite a leitura de seu `SKILL.md`.

| Skill | Quando usar |
| --- | --- |
| `commit-message` | Criar mensagens de commit específicas e padronizadas |
| `java-spring-basics` | Desenvolver Java e Spring Boot no padrão do projeto |
| `git-pr-workflow` | Criar branch, preparar commits e abrir PR |
| `jira-task-planning` | Transformar tarefas do Jira em pequenos passos |

Exemplo de pedido:
> Vou trabalhar na SCRUM-22. Use jira-task-planning e java-spring-basics para me orientar, respeitando AGENTS.md. No fim, sugira como validar manualmente e uma mensagem de commit com commit-message.

## Compatibilidade
As skills usam `.agents/skills/<nome>/SKILL.md` para agentes que reconheçam esse formato. Há instruções adicionais para GitHub Copilot em `.github/copilot-instructions.md`. Caso um agente não carregue skills automaticamente, peça para ler o arquivo diretamente.

## Limites
Agentes podem errar; confira o diff antes de commitar. Não permita que o agente envie credenciais, altere `develop` ou faça merge sem autorização. O professor deve revisar a abordagem, principalmente se ela envolver conteúdo não ensinado.

Estas orientações evoluem com o curso: automação, testes e CI poderão ser introduzidos **depois**, em PR próprio.
