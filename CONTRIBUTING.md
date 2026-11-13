# Guia de Contribuição - Organizows Server

Este documento define as diretrizes e padrões de contribuição para o desenvolvimento do **Organizows Server**, garantindo a rastreabilidade das alterações, qualidade de código e alinhamento da equipe através de revisão cruzada.

---

## 1. Fluxo de Trabalho (Git Workflow)

Adotamos um fluxo baseado em branches de funcionalidade integradas à branch de desenvolvimento:

* **`main`**: Código estável e pronto para produção.
* **`develop`**: Branch principal de integração e desenvolvimento contínuo.
* **Branches de funcionalidade / correção**: Criadas a partir de `develop` e mescladas de volta via **Pull Request (PR)** após revisão cruzada.

### Nomenclatura de Branches
As branches devem conter a chave do ticket Scrum correspondente:
* **Novas funcionalidades**: `feat/SCRUM-<numero>-descricao-curta` ou `SCRUM-<numero>-nome-descricao`
* **Correções de bugs**: `fix/SCRUM-<numero>-descricao-curta`
* **Documentação / Configuração**: `docs/SCRUM-<numero>-descricao-curta` ou `chore/SCRUM-<numero>-descricao-curta`

Exemplo:
```bash
git checkout -b feat/SCRUM-28-rastreabilidade-commits-e-revisao
```

---

## 2. Convenção de Commits (Conventional Commits + SCRUM)

Todos os commits devem seguir a especificação do **[Conventional Commits](https://www.conventionalcommits.org/)** combinada com a identificação obrigatória da tarefa Scrum `[SCRUM-N]`.

### Formato Obrigatório

```text
<tipo>(<escopo opcional>): [SCRUM-N] <descrição concisa no imperativo>

[corpo opcional com detalhes adicionais]

[rodapé opcional com referências ou breaking changes]
```

### Tipos Permitidos

| Tipo | Descrição |
| :--- | :--- |
| `feat` | Uma nova funcionalidade adicionada ao sistema. |
| `fix` | Correção de um bug ou erro. |
| `docs` | Alterações exclusivas na documentação. |
| `style` | Alterações de formatação, pontuação, sem alteração de lógica de código. |
| `refactor` | Refatoração de código que não altera comportamento nem adiciona funcionalidade. |
| `perf` | Melhoria de desempenho. |
| `test` | Adição ou correção de testes automatizados. |
| `build` | Alterações que afetam o sistema de build ou dependências (ex.: `pom.xml`, Maven). |
| `ci` | Alterações em scripts ou configurações de integração contínua. |
| `chore` | Tarefas rotineiras, ferramentas, manutenção de infraestrutura sem impacto no código. |

### Exemplos Válidos

* `feat(materiais): [SCRUM-6] adicionar cadastro de materiais e validacao de saldo`
* `fix(auth): [SCRUM-15] corrigir expiracao de token jwt`
* `docs(contribuicao): [SCRUM-28] documentar guia de contribuicao e padrao de commits`
* `test(localizacao): [SCRUM-18] adicionar testes unitarios para LocalizacaoRepository`

---

## 3. Pull Requests (PRs) e Critérios de Integração

Nenhum desenvolvedor deve fazer commit direto ou merge direto na branch `develop` ou `main`. Todo código entra via Pull Request.

### Requisitos do Pull Request
Ao abrir um PR para `develop`, o autor **deve**:
1. Utilizar o template padrão de PR (`.github/pull_request_template.md`).
2. Indicar o link direto para a tarefa correspondente no Jira / Scrum Board.
3. Descrever claramente o objetivo e o que foi implementado.
4. Anexar **evidência básica de teste** (logs do Maven, prints de testes executados, resposta de endpoints no Postman/Swagger, etc.).
5. Confirmar que a suíte de testes locais foi executada com sucesso (`mvn test` ou `./mvnw test`).

---

## 4. Revisão Cruzada de Integração (Code Review)

O merge em `develop` **exige obrigatoriamente a revisão e aprovação de outro integrante da equipe**.

### Checklist do Autor antes de solicitar revisão:
- [ ] O código segue as convenções e padrões do projeto.
- [ ] As mensagens de commit contêm o identificador `[SCRUM-N]`.
- [ ] O template de Pull Request foi completamente preenchido com descrição e evidência de testes.
- [ ] O projeto compila sem erros e todos os testes estão passando (`./mvnw clean test`).
- [ ] Não foram incluídos arquivos temporários, senhas, chaves de API ou código comentado.

### Checklist do Revisor (Revisão Cruzada):
- [ ] O código atende aos critérios de aceite definidos na tarefa Scrum.
- [ ] A arquitetura e boas práticas do Spring Boot e Java foram respeitadas.
- [ ] A evidência de testes é válida e suficiente para comprovar o funcionamento.
- [ ] O código é legível, manutenível e não apresenta riscos óbvios de segurança.
- [ ] Aprovação explícita concedida no Pull Request.
