# Migration Quality — diagnóstico docente

A CI analisa as migrations SQL em `src/main/resources/db/migration` de forma
automática nos PRs para `develop` e `main`, nos pushes dessas branches e em
execuções manuais. Não utiliza credenciais, banco de produção ou serviços pagos.
Não modifica o Flyway, o POM, as migrations existentes nem exige testes/cobertura.

## Como interpretar os checks

- **Convenção e histórico**: procura nomes fora do padrão didático
  `V1__cria_tabela.sql` (também permite versões numéricas segmentadas e
  `R__descricao.sql` para repeatables), versões numéricas equivalentes, novas
  versões anteriores à maior versão da base e edição, remoção ou renomeação
  de scripts versionados. No PR, usa a base real e a árvore de merge prevista.
  Versões não precisam ser consecutivas. Repeatables podem ser atualizadas.
- **SQLFluff**: feedback de formatação, palavras-chave e parsing SQL no
  dialeto ANSI, com anotações no check. H2 não tem dialeto específico;
  falsos positivos são possíveis. Não altera ou formata os arquivos.
- **Squawk (PostgreSQL)**: aponta riscos de alterações DDL, como exclusão
  de colunas, mudanças incompatíveis e locks. Consulte o log do check.
  A análise assume transações e pode sinalizar sintaxe exclusiva de H2 ou
  operações aceitáveis em tabelas novas. É sempre informativa.
- **Execução real**: o check existente de build e inicialização aplica o
  Flyway em H2 temporário e usa Hibernate `ddl-auto=validate`.
  É uma validação do esquema para começar do zero, não de upgrade com dados
  existentes, nem homologação em PostgreSQL.

## Formatação para próximas migrations

Use palavras-chave SQL em maiúsculas, identificadores no padrão existente,
uma instrução por bloco e indentação de quatro espaços dentro de expressões.
O SQLFluff também espera espaço antes do parêntese que lista colunas no exemplo
abaixo (`material (nome)`).

```sql
CREATE INDEX idx_material_nome
ON material (nome);

ALTER TABLE material ADD CONSTRAINT chk_material_status
CHECK (status IN (
    'DISPONIVEL',
    'EM_MANUTENCAO',
    'OBSOLETO',
    'INDISPONIVEL'
));
```

Os apontamentos `LT01` e `LT02` são de espaçamento e indentação: não indicam
que os valores ou a regra do `CHECK` estão incorretos. Como o SQLFluff usa o
dialeto ANSI, o aviso `PRS` em uma migration com sintaxe específica do H2 pode
ser incompatibilidade do parser, não erro de execução no H2. Confirme a sintaxe
no banco adotado pelo projeto antes de alterar uma migration.

No Squawk, `constraint-missing-not-valid` alerta que adicionar um `CHECK` a uma
tabela existente examina os registros antigos e pode bloquear gravações durante
a verificação. `require-lock-timeout` e `require-statement-timeout` apontam a
ausência desses limites; `require-concurrent-index-creation` alerta sobre a
criação normal de índice em PostgreSQL; `prefer-text-field` questiona o uso de
`VARCHAR` com tamanho. São diagnósticos para avaliar com o banco e o volume de
dados reais, não instruções para mudar tipos, dados ou constraints apenas para
silenciar o linter. Antes de propor um novo `CHECK`, consulte os dados com uma
busca somente de leitura e confira o histórico Flyway do ambiente; não use
`repair` automático nem reescreva migrations já aplicadas.

## Regularização inicial e evolução

Durante a regularização do versionamento, realizada separadamente pela equipe,
nomes, versões e mudanças no histórico são **avisos**, não bloqueios adicionais.
Não renomeie, altere ou apague scripts já aplicados sem antes avaliar
`flyway_schema_history` nos ambientes existentes; não automatize `repair`.

Quando a regularização estiver revisada e os bancos existentes compatíveis,
o docente poderá ativar `MIGRATION_STRICT=true` nas variáveis de Actions
para transformar apenas os problemas de convenção/histórico em falhas de CI.
A configuração Spring `spring.flyway.validate-migration-naming=true` pode ser
adotada depois da mesma regularização, para falhar ao iniciar a aplicação
com nomes inválidos. A CI de lint permanece informativa. Não configure novos
checks obrigatórios antes da homologação.

Posteriormente, testar instalação e atualização em banco populado com dados
fictícios, usando a versão de PostgreSQL efetivamente adotada pelo projeto.
O lint não garante segurança de execução, integridade dos dados ou ausência
de locks.

Referências:
- https://documentation.red-gate.com/flyway/reference/commands/validate
- https://docs.sqlfluff.com/en/stable/usage/ci-cd
- https://squawkhq.com/docs/
