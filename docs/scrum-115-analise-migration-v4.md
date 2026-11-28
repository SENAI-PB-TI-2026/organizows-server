# SCRUM-115 — análise da migration V4

Migration analisada: `src/main/resources/db/migration/V4__add_material_technical_attributes.sql`.

## Correspondência SQL e Java

| Campo | Valores permitidos no CHECK SQL | Enum Java | Resultado |
| --- | --- | --- | --- |
| `categoria` | `PERIFERICO`, `HARDWARE`, `CABO`, `COMPONENTE_ELETRONICO`, `EQUIPAMENTO`, `FERRAMENTA`, `CONSUMIVEL` | `CategoriaMaterial` | Correspondência exata: 7 valores |
| `status` | `DISPONIVEL`, `EM_MANUTENCAO`, `OBSOLETO`, `INDISPONIVEL` | `StatusMaterial` | Correspondência exata: 4 valores |

Após a SCRUM-116, `MaterialCadastroBaseDTO` declara `categoria` e `status` como
enums e exige ambos com `@NotNull`. `MaterialRequestDTO` herda esses campos e
acrescenta `quantidadeDisponivel`; `MaterialUpdateRequestDTO` também herda os
campos comuns, sem o saldo inicial. `Material` persiste os enums como texto
(`EnumType.STRING`) e as colunas já são `NOT NULL` desde a V2. Assim, os CHECKs
da V4 complementam as validações da API e protegem gravações que não passem
pelo serviço Java. As regras de grupo e subgrupo são validadas pelo
`MaterialService`; elas não fazem parte dos CHECKs de categoria/status.

## Validação manual com dados fictícios

Em um banco H2 temporário, os SQLs das migrations V1, V2 e V3 foram executados
em ordem; depois foi inserido um material fictício válido antes de executar o
SQL da V4. A V4 foi executada com esse registro existente. Depois:

- 28 combinações (todas as 7 categorias com cada um dos 4 status) foram aceitas;
- `CATEGORIA_INVALIDA` foi rejeitada pelo banco (SQLState `23513`);
- `STATUS_INVALIDO` foi rejeitado pelo banco (SQLState `23513`);
- as 29 linhas fictícias válidas permaneceram gravadas.

O banco, os scripts e o verificador temporários foram removidos ao fim da
execução. Essa verificação cobre a migration no H2 e os valores fictícios
usados; não consulta nenhum banco compartilhado nem confirma os dados legados
de outros ambientes. A comparação SQL/Java não apontou inconsistência que peça
uma nova migration; a conferência de dados reais ainda depende de consulta
autorizada no ambiente correspondente. V4 permaneceu intacta.

Antes de adicionar CHECKs semelhantes em um banco com dados existentes, faça
uma consulta somente de leitura para localizar valores que seriam rejeitados,
por exemplo:

```sql
SELECT categoria, COUNT(*)
FROM material
WHERE categoria IS NULL
   OR categoria NOT IN (
       'PERIFERICO', 'HARDWARE', 'CABO', 'COMPONENTE_ELETRONICO',
       'EQUIPAMENTO', 'FERRAMENTA', 'CONSUMIVEL'
   )
GROUP BY categoria;

SELECT status, COUNT(*)
FROM material
WHERE status IS NULL
   OR status NOT IN ('DISPONIVEL', 'EM_MANUTENCAO', 'OBSOLETO', 'INDISPONIVEL')
GROUP BY status;
```

Essas consultas precisam ser executadas no ambiente autorizado antes de uma
nova restrição. Os resultados do H2 fictício não substituem essa conferência.

## Alertas de qualidade observados

No workflow `CI docente - Migration Quality`, a análise da V4 registrou:

- SQLFluff `LT01` na separação entre tabela e lista de colunas e `LT02` na
  indentação do bloco dos CHECKs; são avisos de formatação, não divergências
  entre os enums;
- Squawk `constraint-missing-not-valid` nos dois CHECKs, indicando varredura
  dos registros existentes e possível bloqueio de gravações durante a
  validação;
- avisos gerais de timeout, índice e `VARCHAR`, conforme explicado em
  [Migration Quality](../.github/MIGRATION-QUALITY.md#como-interpretar-os-checks).

Os jobs de lint são informativos e terminaram com sucesso. Não reformatar a V4
já versionada para eliminar avisos; os exemplos de formatação para novas
migrations estão no guia de qualidade. O exemplo foi verificado localmente com
SQLFluff 4.5.0 e as regras CP01, LT01, LT02 e LT12 usadas pelo workflow; não
recebeu apontamentos.
