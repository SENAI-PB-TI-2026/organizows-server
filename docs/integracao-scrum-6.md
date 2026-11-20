# Catálogo e saldo de materiais — PR #4

## Contrato da API

O cadastro inicial usa `MaterialRequestDTO` e recebe o saldo inicial:

```http
POST /api/v1/materiais
```

```json
{
  "codigo": "MAT-001",
  "nome": "Cabo de rede",
  "descricao": "Cabo azul Cat6",
  "caracteristicas": "305 metros",
  "modelo": "Cat6",
  "especificacoesTecnicas": "4 pares trançados",
  "grupo": "Cabos",
  "subgrupo": "Rede",
  "categoria": "CABO",
  "status": "DISPONIVEL",
  "localId": 1,
  "quantidadeDisponivel": 20,
  "quantidadeMinima": 5
}
```

A atualização cadastral usa `MaterialUpdateRequestDTO`, que **não possui `quantidadeDisponivel`**. O saldo atual é preservado; `quantidadeMinima` continua editável:

```http
PUT /api/v1/materiais/{id}
```

```json
{
  "codigo": "MAT-001",
  "nome": "Cabo de rede",
  "descricao": "Cabo azul Cat6",
  "caracteristicas": "305 metros",
  "modelo": "Cat6",
  "especificacoesTecnicas": "4 pares trançados",
  "grupo": "Cabos",
  "subgrupo": "Rede",
  "categoria": "CABO",
  "status": "DISPONIVEL",
  "localId": 1,
  "quantidadeMinima": 8
}
```

O saldo só muda pelas operações específicas:

- `POST /api/v1/materiais/{id}/ajuste-saldo` com `{"deltaQuantidade": 5}`;
- `PATCH /api/v1/materiais/{id}/saldo?delta=5` (também aceita o delta no corpo).

Swagger/OpenAPI deriva os campos dos DTOs dos endpoints. A descrição é obrigatória: a validação de entrada limita-a a 150 caracteres, a entidade JPA declara `nullable = false` e a migration V2 mantém `NOT NULL`. Falhas de validação da requisição retornam 400; violações de integridade do banco são convertidas pelo handler existente em 409.

## Escopo e situação por cartão

| Cartão | Cobertura nesta branch | Pendente |
|---|---|---|
| SCRUM-6 | Código único, nome, grupo, características, modelo, quantidade, localização canônica e validação de chave existente. | Nenhum critério original identificado como pendente. |
| SCRUM-11 | CRUD, descrição/características, localização, consulta e distinção por modelo/especificações. | Foto ou URL de imagem não existe; cartão permanece parcial. |
| SCRUM-12 | Quantidade mínima configurável; alerta quando disponível `<` mínimo; igualdade ou valor maior não alerta; ajuste recalcula o alerta na resposta/consulta. | A cobertura não conclui os dependentes SCRUM-13 e SCRUM-23 nem fluxo de notificação; cartão permanece parcial. |
| SCRUM-13 | Ajuste transacional com bloqueio pessimista, delta zero rejeitado e saldo negativo recusado. | Não existe lançamento/histórico de movimentação atômico com o saldo; critério correspondente e dependências SCRUM-22/SCRUM-26 permanecem pendentes. |
| SCRUM-21 | Categoria é enum e o material possui grupo e subgrupo textuais; busca por grupo está disponível. | Catálogo hierárquico e validação de que o subgrupo pertence à categoria não estão implementados; cartão permanece parcial. |

## Validação

- `./mvnw.cmd clean test`, executado após estas correções com Java 25.0.1: 99 testes, 0 falhas, 0 erros e 0 ignorados.
- Flyway aplicou V1–V4 em H2 vazio e Hibernate validou o mapeamento JPA durante a suíte.
- Banco-alvo persistente: conferir dados existentes e checksums antes de aplicar V4; não reescrever migrações já aplicadas nem executar `flyway repair` sem análise.
- PostgreSQL não foi validado nesta revisão; a configuração local de testes usa H2.

## Integrações e limites

- `Material` usa `Localizacao` e a FK `material.localizacao_id`, conforme a integração de SCRUM-18.
- O módulo `Peca` do PR #2 não foi alterado. Até decisão da equipe sobre a relação entre peça e material, seus registros e saldos continuam separados.
- Este PR não declara SCRUM-11, SCRUM-12, SCRUM-13 ou SCRUM-21 concluídas.
