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
3. Escreva seu pedido normalmente, por exemplo: `Implemente a SCRUM-22` ou `Sugira a mensagem do meu commit`. As instruções permanentes orientam o agente a selecionar a skill apropriada automaticamente. Não é necessário conhecer os nomes das skills.

| Skill | Quando usar |
| --- | --- |
| `commit-message` | Criar mensagens de commit específicas e padronizadas |
| `java-spring-basics` | Desenvolver Java e Spring Boot no padrão do projeto |
| `git-pr-workflow` | Criar branch, preparar commits e abrir PR |
| `jira-task-planning` | Transformar tarefas do Jira em pequenos passos |

Exemplo de pedido:
> Vou trabalhar na SCRUM-22. Analise os critérios da tarefa, implemente apenas o que já foi ensinado e sugira como conferir manualmente e qual mensagem de commit utilizar.

## Compatibilidade
As skills usam `.agents/skills/<nome>/SKILL.md` para agentes que reconheçam esse formato. Há instruções adicionais para GitHub Copilot em `.github/copilot-instructions.md`. Os agentes geralmente leem as instruções permanentes automaticamente e selecionam skills conforme a solicitação, mas **não há garantia de leitura integral de todas as skills em cada mensagem**. Caso não reconheçam uma skill, peça para ler o arquivo diretamente. Abra a **raiz do repositório** como workspace e reinicie a sessão do agente após atualizar os arquivos, se necessário.

## Limites
Agentes podem errar; confira o diff antes de commitar. Não permita que o agente envie credenciais, altere `develop` ou faça merge sem autorização. O professor deve revisar a abordagem, principalmente se ela envolver conteúdo não ensinado.

Estas orientações evoluem com o curso: automação, testes e CI poderão ser introduzidos **depois**, em PR próprio.

## Conteúdo ainda não ministrado: login e permissões
Nenhuma equipe trabalhou ainda com **autenticação ou autorização**. Portanto, os agentes não devem incluir espontaneamente login, JWT, Spring Security, usuários/senhas ou perfis. Tickets dessa área continuam válidos no Jira, mas devem aguardar orientação do professor antes da implementação. Essa restrição é **pedagógica e temporária**, não uma recomendação de operar sistemas reais sem segurança. Não publicar APIs de escrita abertas na internet.

### Boas práticas aproveitadas de outra equipe
Foram identificadas instruções de agentes do repositório `minha-sala-server` (branch `feat/SCRUM-216`): divisão de responsabilidades por camada, convenção de verbos em commits, roteiro CRUD, explicitação de dúvidas importantes e autorização humana para operações Git. Aplicar essas ideias adaptadas ao OrganizoWS, sem copiar proibições de domínio de outro projeto ou introduzir exigências de testes automatizados nesta etapa.
