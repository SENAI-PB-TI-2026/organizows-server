# Guia de Contribuição — Organizows Server

Este projeto é desenvolvido por alunos. O objetivo é praticar organização de código, Git, colaboração e documentação de entregas. Utilize também `AGENTS.md` e as skills em `.agents/skills/`.

## 1. Branches
- `main`: versão de referência publicada.
- `develop`: integração das atividades.
- Cada tarefa é desenvolvida em uma branch criada a partir de `develop`, com chave Jira: `feat/SCRUM-22-cadastro-material`, `fix/SCRUM-19-erro-consulta` ou `docs/SCRUM-28-guia`.
- Não faça commit direto em `develop` e `main`. Abra PR para `develop` e peça revisão cruzada.

## 2. Commits
Formato obrigatório:
```text
tipo(escopo): [SCRUM-N] descrição objetiva
```

Tipos mais usados:
- `feat`: nova função;
- `fix`: correção;
- `docs`: documentação;
- `refactor`: organização do código sem mudar a função;
- `style`: formatação sem mudar o comportamento;
- `chore`: manutenção;
- `build`: mudança no processo de compilação/dependência.
Outros tipos de Conventional Commits podem ser utilizados quando fizerem sentido.

Exemplos:
```text
feat(materiais): [SCRUM-22] adicionar cadastro de materiais
fix(localizacao): [SCRUM-19] tratar codigo inexistente
docs(readme): [SCRUM-28] documentar convencoes do projeto
```

Evite mensagens `ajustes`, `final teste`, `pronto` ou `mudanças`. Cada commit deve conter alterações relacionadas à mensagem. Consulte a skill `commit-message` antes de finalizar.

## 3. Como programar
- Leia o código semelhante já existente e siga a organização `model`, `repository`, `service`, `controller` e `dto`.
- Use nomes claros; evite duplicação de código e responsabilidades misturadas.
- Não modifique `pom.xml`, a versão Java ou configurações centrais sem necessidade e orientação.
- Valide entradas e situações de erro, conforme requisitos do cartão.
- Não adicione senhas, arquivos locais, código temporário ou mudanças de outro trabalho ao commit.

## 4. Verificação nesta fase
**Testes automatizados não são exigidos nesta etapa do curso.** O aluno deve verificar manualmente o que implementou, quando o ambiente permitir. Exemplos: iniciar a aplicação, chamar endpoint pelo Swagger/Postman, confirmar resultado esperado e testar um caso inválido.

No PR, documente **o que foi efetivamente verificado**. Se não conseguiu executar, descreva o impedimento sem inventar resultados. Requisitos de testes automatizados e CI só entram quando ensinados ou explicitamente solicitados pelo professor.

## 5. Pull Requests
Para enviar uma atividade:
1. Abra PR de sua branch para `develop` usando `.github/pull_request_template.md`.
2. Inclua cartão Jira, resumo do trabalho e instruções de verificação manual.
3. Revise `git diff` e `git status` e retire arquivos fora do escopo.
4. Solicite revisão por outro integrante. A aprovação por outro colega é regra de equipe; sua obrigatoriedade automática requer configuração de branch protection/ruleset no GitHub.
5. Só realize merge após aprovação e autorização.

## 6. Critérios para revisão
- Entrega atende ao Jira?
- Código é compreensível e segue os exemplos do projeto?
- O PR documenta o que foi implementado e como conferir?
- Não foram incluídos segredos, mudanças de IDE ou alterações sem relação com o ticket?
- O autor explicou erros, limitações ou pontos incompletos?

Não confunda o funcionamento manual com a garantia de prontidão para produção: testes e verificações adicionais serão introduzidos ao longo do curso.

## 7. Escopo pedagógico de autenticação/autorização
Ainda não foram ministrados login, autenticação, autorização e papéis de usuário. Não adicione Spring Security, JWT, tokens, senha ou controle de permissões como parte de atividades comuns. Caso um cartão do Jira trate especificamente desses conteúdos, consulte o professor antes de começar; os cartões não devem ser considerados automaticamente concluídos ou excluídos. O protótipo sem controles de acesso **não deve ser exposto como aplicação de produção**.
