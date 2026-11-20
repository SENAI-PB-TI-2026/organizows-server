# Integração de materiais — PR #4 / SCRUM-6

## Alterações desta revisão

- O `Material` continua relacionado à entidade `Localizacao` compartilhada pela implementação de SCRUM-18, pela FK `material.localizacao_id`. Cadastro rejeita localização ausente ou inexistente; `Local` concorrente e segunda V1 não existem no diff atual do PR.
- O cadastro aceita e persiste código único, nome, descrição, características, grupo/subgrupo, categoria/status, modelo, especificações técnicas, localização e quantidades. A resposta de criação e consulta expõe também `modelo` e `especificacoesTecnicas`, permitindo distinguir componentes semelhantes; inclui ainda nome/endereço hierárquico da localização.
- Campos técnicos foram adicionados em V4 aditiva; V1–V3 não foram alteradas nesta revisão. V4 também restringe valores de categoria/status aos enums da API.
- Quantidades não negativas são validadas nos DTOs e protegidas por CHECKs V3. Ajuste de saldo é transacional, usa lock pessimista, rejeita delta nulo/zero, saldo negativo e overflow. O PUT cadastral preserva saldo.
- O alerta dispara somente quando `quantidadeDisponivel < quantidadeMinima`, conforme SCRUM-12 (“abaixo do mínimo”); igualdade não dispara.
- O ajuste permanece disponível em `POST /api/v1/materiais/{id}/ajuste-saldo`; `PATCH /api/v1/materiais/{id}/saldo` usa a mesma operação para compatibilidade com o contrato já exercitado nos testes.

## Validação executada

- `java -version`: Java 25.0.1.
- Última execução anterior a esta revisão do DTO: `./mvnw clean verify` (Windows: `./mvnw.cmd clean verify`), 96 testes, 0 falhas, 0 erros, 0 ignorados. Essa execução não inclui as novas asserções de `modelo` e `especificacoesTecnicas` adicionadas agora; os testes precisam ser executados novamente.
- Os testes Flyway partiram de H2 vazio e aplicaram V1–V4; Hibernate validou o esquema e a suíte incluiu testes de vínculo, validação HTTP, saldo e concorrência. Os testes de controller foram atualizados para conferir os campos técnicos no cadastro e nas consultas por ID e código.
- `git diff --check`: sem erros de whitespace.
- Maven emitiu avisos de acesso 401 a metadados de repositórios opcionais e aviso de que Flyway ainda não verificou H2 2.4.240; os artefatos necessários estavam disponíveis e o build terminou com sucesso.

## Pendências e limites de escopo

- SCRUM-21 exige hierarquia de grupos/subgrupos e valores válidos, mas os cartões consultados não definem quais grupos/subgrupos são válidos. A implementação ainda guarda grupo/subgrupo como texto; é necessária decisão da equipe antes de declarar a hierarquia completa.
- O enum atual oferece categorias de ferramentas, equipamentos e componentes, mas não valida vínculo entre um subgrupo e a categoria. Esse critério permanece parcial até a decisão do catálogo.
- SCRUM-11 menciona imagem/URL; esse atributo não foi incluído nesta entrega. CRUD e busca por código/grupo cobrem apenas parte dos cartões dependentes.
- Não há entidade/histórico de movimentações. O saldo é ajustado, mas SCRUM-13 não está concluída; o histórico pertence à SCRUM-26 e não foi inventado aqui.
- O módulo `Peca` do PR #2 não está na `develop` consultada e não foi alterado. Na branch existente do PR #2, `Peca` é uma entidade separada (`tb_pecas`) com categoria/descrição, estoque próprio e localização em texto; não há vínculo explícito com `Material`. Até a equipe definir se peça é subtipo de material ou cadastro distinto, este PR mantém os modelos separados e não sincroniza seus saldos/localizações.
- Não se confirmou se V2/V3 foram aplicadas em algum banco compartilhado. Não as reescrevi. Antes de promover V4 a um banco persistente, confirme os dados existentes, sobretudo valores de categoria/status antes do novo CHECK. Se V2/V3 já tiverem sido aplicadas com checksums diferentes, não use `repair` nem reescreva histórico: preparar migration corretiva após inspeção dos dados.
- Não houve commit, push, merge/rebase ou alteração no Jira.
