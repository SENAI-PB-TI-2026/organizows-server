# SCRUM-111 — Índices do catálogo e consultas de materiais

## Escopo e fontes examinadas

Esta análise compara as migrations `V2__create_table_material.sql` e `V4__add_material_technical_attributes.sql` com os métodos de `MaterialRepository`, `MaterialService` e `MaterialController`. Nenhuma migration existente foi alterada, renomeada ou removida.

O controller oferece listagem sem filtro, listagem filtrada por `grupo`, consulta por `id` e consulta por `codigo`. O método `findByNomeContainingIgnoreCase` existe no repository, mas não é chamado pelo controller ou service. `findByLocalizacaoId` também não é chamado; `existsByLocalizacaoId` é usado por `LocalizacaoService` para verificar materiais associados antes de excluir uma localização.

## Índices encontrados e relação com as consultas

| Índice no material | Origem | Uso observado ou esperado |
|---|---|---|
| `material_pkey (id)` | Chave primária | Atende à consulta de material por ID. |
| `uk_material_codigo (codigo)` | Restrição `UNIQUE` de V2 | Atende à busca por código e também garante unicidade. Não há outro índice sobre `codigo`. |
| `idx_material_localizacao (localizacao_id)` | V2 | Atende ao `existsByLocalizacaoId` usado pela regra de exclusão de localização e a uma consulta exata por localização se for chamada. Uma chave estrangeira não cria automaticamente um índice na coluna que a referencia. |
| `idx_material_grupo (grupo)` | V2 | Atende a igualdade sensível a maiúsculas/minúsculas. O endpoint usa `findByGrupoIgnoreCase`, que compara a expressão `upper(grupo)`; o índice simples não atende a essa expressão. |
| `idx_material_nome (nome)` | V4 | Atende a igualdade exata por nome, mas não foi encontrada uma consulta ativa que faça essa igualdade. O método existente usa busca parcial sem diferenciar caixa e tem comportamento diferente. |

Os índices da tabela têm colunas ou funções distintas; a lista em `pg_indexes` não mostrou definições duplicadas. A restrição única de código já é respaldada por `uk_material_codigo`, então adicionar outro índice simples em `codigo` duplicaria o mesmo propósito.

## Evidência de planos no PostgreSQL

Foi iniciado PostgreSQL **17.12** em um container Docker descartável, sem volume persistente e sem porta publicada. As migrations V1–V4 foram aplicadas diretamente nessa base vazia. Foram inseridas 50 localizações e 100.000 materiais fictícios, distribuídos por sete grupos; depois foi executado `ANALYZE`. Os planos abaixo vieram de `EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)`. São observações desta carga sintética, não previsão de desempenho de uma base real.

| Consulta examinada | Plano observado | Leitura |
|---|---|---|
| `codigo = 'MAT-090001'` | `Index Scan` em `uk_material_codigo`; 1 linha; 0,032 ms | O índice único existente é escolhido. |
| `nome = 'Item-090001'` | `Index Scan` em `idx_material_nome`; 1 linha; 0,050 ms | O índice V4 serve para igualdade exata, embora esse caminho não exista na API atual. |
| `upper(nome) LIKE upper('%090001%')` | `Seq Scan`; 1 linha; 43,250 ms | Representa a forma de busca parcial case-insensitive do método `findByNomeContainingIgnoreCase`. Um B-tree simples em `nome` não atende a expressão nem ao curinga inicial. Este método não está conectado a uma rota hoje. |
| `upper(grupo) = upper('Ferramentas')` | `Seq Scan`; 14.285 de 100.000 linhas; 41,695 ms | Representa o filtro case-insensitive ativo do catálogo. O B-tree simples de `grupo` não foi escolhido porque a consulta transforma a coluna. |
| `grupo = 'Ferramentas'` | `Bitmap Heap Scan` com `Bitmap Index Scan` em `idx_material_grupo`; 14.285 linhas; 18,564 ms | Confirma que o índice V2 atende à igualdade simples; não é a expressão usada por `findByGrupoIgnoreCase`. |
| `EXISTS (... localizacao_id = 1)` | `Index Only Scan` em `idx_material_localizacao`; sem busca no heap; 0,079 ms | Corresponde à verificação booleana usada para impedir exclusão de localização com materiais associados. |

Como experimento apenas nessa base descartável, foi criado temporariamente um índice de expressão em `upper(grupo)`. O mesmo filtro case-insensitive passou a `Bitmap Heap Scan` com `Bitmap Index Scan`, retornando as mesmas 14.285 linhas em 11,898 ms. O índice de prova foi removido ao final do experimento. O resultado sugere que uma expressão indexada pode ajudar nessa carga, mas não mede o custo adicional de escrita nem representa o volume ou frequência reais do sistema.

## Squawk e criação de índices no PostgreSQL

O workflow do projeto executa Squawk 2.70.0 em modo informativo. A execução local do mesmo CLI e das migrations V1–V4 reproduziu em `V4__add_material_technical_attributes.sql:3` o aviso `require-concurrent-index-creation` para `CREATE INDEX idx_material_nome ON material(nome)`. O script V4 atua sobre uma tabela criada em V2; por isso, é diferente de criar índices junto da tabela nova em V2. A execução também apontou outros avisos de lint nas migrations; eles não foram tratados nesta tarefa.

No PostgreSQL, `CREATE INDEX` normal permite leituras, mas bloqueia inserções, atualizações e exclusões na tabela durante a construção. `CREATE INDEX CONCURRENTLY` evita esse bloqueio de escrita, porém realiza mais trabalho, não pode rodar dentro de uma transação e tem outras etapas e limitações. O ajuste passa a ser necessário quando a tabela já tem volume ou tráfego suficiente para que esse bloqueio seja inaceitável; em tabelas pequenas e sem escritores concorrentes, o modo normal pode ser adequado. Como o projeto também aplica migrations em H2, não se deve copiar `CONCURRENTLY` para uma migration compartilhada sem validar suporte e configuração transacional do Flyway em ambos os bancos.

## Decisão

- Manter V1–V4 intactas: elas podem já estar aplicadas e o ticket não justificou corrigir o histórico.
- Não adicionar migration nesta tarefa. A expressão `upper(grupo)` melhorou o plano na carga fictícia, mas o benefício no volume real, o custo nas escritas e a compatibilidade de uma migration com H2 ainda precisam de validação antes de reservar uma nova versão.
- Não adicionar índice para a busca por nome: a busca parcial não usa o B-tree atual, e essa busca ainda não é exposta pelo controller. Antes de indexá-la, seria necessário decidir o contrato da busca e medir o padrão e volume reais.
- Não remover `idx_material_nome` ou `idx_material_grupo`: não há evidência suficiente de benefício líquido ao removê-los; ambos têm pelo menos um formato de consulta distinto que podem atender. A eventual limpeza deve ser uma nova migration, nunca edição de V2/V4, após conferir o histórico Flyway dos ambientes.
- Antes de evoluir esse esquema em PostgreSQL populado, avaliar o tamanho e os escritores concorrentes. Se a construção bloquear escritas por tempo inaceitável, estudar uma migration isolada e não transacional com `CREATE INDEX CONCURRENTLY`, incluindo validação específica de H2/Flyway.

## Verificação e limites

| Critério | Evidência | Situação |
|---|---|---|
| Registrar consultas, índices e evidência de execução | Mapeamento do código e planos PostgreSQL 17.12 com dados fictícios | Registrado; carga descartável de 100.000 materiais |
| Justificar manutenção ou melhoria e só criar migration com benefício e compatibilidade | Comparação da expressão `upper(grupo)` com índice temporário; custo e compatibilidade de escrita ainda não comprovados | Decisão registrada; nenhuma migration nova |
| Preservar endpoints e migrations antigas | Nenhum controller, repository ou arquivo de migration foi alterado | Preservados; aplicação HTTP não foi iniciada porque não houve mudança nesses componentes |

As migrations foram aplicadas diretamente, sem Flyway; portanto, a base descartável não contém um histórico Flyway representativo. Nenhum banco compartilhado, credencial real ou dado de produção foi acessado. O container descartável foi removido após a captura desta evidência.

Referências:

- [PostgreSQL 17 — CREATE INDEX e criação concorrente](https://www.postgresql.org/docs/17/sql-createindex.html)
- [PostgreSQL 17 — restrições e índices de chaves estrangeiras](https://www.postgresql.org/docs/17/ddl-constraints.html)
- [Squawk — require-concurrent-index-creation](https://squawkhq.com/docs/require-concurrent-index-creation)
- [Política Migration Quality do repositório](../.github/MIGRATION-QUALITY.md)
