# Skills compartilhadas — Organizows Server

Estas skills ficam versionadas no repositório para que quem clonar o projeto tenha as mesmas instruções. Não é necessário copiá-las para diretórios pessoais.

## Compatibilidade
- Codex e Google Antigravity: diretório `.agents/skills/<nome>/SKILL.md`.
- GitHub Copilot: suporte a skills compatíveis; este repositório também inclui `.github/copilot-instructions.md`.
- `AGENTS.md` contém regras permanentes; cada skill fornece instruções **sob demanda**, reduzindo contexto e repetição.

## Como usar
1. Faça `git pull` da branch que já contém as skills (após merge em `develop`).
2. Abra **a raiz** de `organizows-server` no editor com o agente; reinicie a sessão do agente se as skills não aparecerem.
3. Descreva a tarefa com chave Jira e critérios. Exemplo: "Implemente SCRUM-22 seguindo jira-task-delivery, flyway-data-integrity e test-and-verify".
4. Em Codex, é possível chamar a skill diretamente quando disponível, por exemplo `$jira-task-delivery`. Em outros agentes, solicite a leitura do `SKILL.md` pertinente.
5. Execute comandos e testes localmente; os agentes não substituem revisão cruzada nem CI.

## Catálogo
| Skill | Uso |
| --- | --- |
| `jira-task-delivery` | Planejamento e aceitação Jira |
| `spring-api-patterns` | API, DTO, service, HTTP |
| `flyway-data-integrity` | JPA, schema, migrations |
| `inventory-concurrency` | Estoque e transações |
| `test-and-verify` | Testes e pré-produção |
| `pull-request-review` | Revisão técnica de PR |

## Política de manutenção
Atualizar as skills em PR com revisão cruzada. A prática documentada não garante qualidade sozinha: regras de branch, CI, testes e avaliação humana continuam necessários. Ajustar conteúdo quando versões, contratos ou modelagem do projeto mudarem.
