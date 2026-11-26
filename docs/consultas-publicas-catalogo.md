# Consultas HTTP do catálogo

Índice integrador das consultas GET encontradas na branch `develop` na preparação desta documentação. Os contratos abaixo refletem os controllers e DTOs existentes; confirme o OpenAPI da aplicação ao integrar mudanças posteriores.

As respostas de sucesso usam `ApiResponse<T>`: `success`, `message`, `data` e `timestamp`. A data e hora são geradas em tempo de execução (ISO-8601). Listas sem resultados retornam `200` com `data: []`.

## Rotas existentes

| Rota GET | Parâmetros | Campos principais de `data` | Resposta e observações |
|---|---|---|---|
| `/api/v1/localizacoes` | Query opcional `codigo` (texto). Se não vazio, filtra por código exato e retorna lista de zero ou um item. | `id`, `codigo`, `nome`, `tipo`, `descricao`, `localPaiId`, `localPaiNome`, `enderecoCompleto` | `200`, lista. Código sem correspondência resulta em lista vazia, não `404`. |
| `/api/v1/localizacoes/raizes` | Nenhum | mesmos campos de localização | `200`, localizações sem pai. |
| `/api/v1/localizacoes/{id}` | Path `id` (inteiro longo) | campos de localização | `200` se encontrada; `400` se `id` não puder ser convertido; `404` se não existir. |
| `/api/v1/localizacoes/codigo/{codigo}` | Path `codigo` (texto) | campos de localização | `200` se encontrada; `404` se não existir. |
| `/api/v1/localizacoes/{id}/filhas` | Path `id` (inteiro longo) | campos de localização | `200`, lista das filhas diretas. ID sem filhas, inclusive inexistente, resulta em lista vazia conforme a consulta atual. `400` se `id` não puder ser convertido. |
| `/api/v1/localizacoes/{id}/caminho` | Path `id` (inteiro longo) | campos de localização, da raiz ao item | `200` se encontrada; `400` se `id` não puder ser convertido; `404` se não existir. |
| `/api/v1/materiais` | Query opcional `grupo` (texto). Vazio ou ausente lista todos; preenchido filtra ignorando maiúsculas/minúsculas. | `id`, `codigo`, `nome`, `descricao`, `caracteristicas`, `modelo`, `especificacoesTecnicas`, `grupo`, `subgrupo`, `categoria`, `status`, `localId`, `localizacaoNome`, `localizacaoEndereco`, `quantidadeDisponivel`, `quantidadeMinima`, `alertaReposicao`, `criadoEm`, `atualizadoEm` | `200`, lista; grupo sem resultados resulta em lista vazia. |
| `/api/v1/materiais/classificacoes` | Nenhum | `categoria`, `grupo`, `subgrupos` | `200`, lista de classificações disponíveis, mesmo sem materiais. |
| `/api/v1/materiais/{id}` | Path `id` (inteiro longo) | campos de material | `200` se encontrado; `400` se `id` não puder ser convertido; `404` se não existir. |
| `/api/v1/materiais/codigo/{codigo}` | Path `codigo` (texto) | campos de material | `200` se encontrado; `404` se não existir. |
| `/api/v1/materiais/alerta-reposicao` | Nenhum | campos de material | `200`, lista de materiais cujo saldo está abaixo do mínimo; sem alertas, lista vazia. |

Rotas de escrita (POST, PUT, PATCH e DELETE) não fazem parte deste índice. Os exemplos abaixo usam valores fictícios apenas para explicar a estrutura.

## Exemplos

### Consulta por localização

`GET /api/v1/localizacoes?codigo=SALA-101`

Resposta `200` (os valores são ilustrativos):

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": [
    {
      "id": 12,
      "codigo": "SALA-101",
      "nome": "Sala 101",
      "tipo": "Sala",
      "descricao": "Sala de exemplo",
      "localPaiId": 3,
      "localPaiNome": "Bloco A",
      "enderecoCompleto": "Bloco A > Sala 101"
    }
  ],
  "timestamp": "2026-11-26T12:00:00"
}
```

### Consulta por material

`GET /api/v1/materiais?grupo=Ferramentas`

Resposta `200` (os valores são ilustrativos):

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": [
    {
      "id": 21,
      "codigo": "MAT-EXEMPLO",
      "nome": "Chave de exemplo",
      "descricao": "Item fictício para documentação",
      "caracteristicas": "Manual",
      "modelo": "Modelo demonstrativo",
      "especificacoesTecnicas": "Não aplicável",
      "grupo": "Ferramentas",
      "subgrupo": "Manuais",
      "categoria": "FERRAMENTA",
      "status": "DISPONIVEL",
      "localId": 12,
      "localizacaoNome": "Sala 101",
      "localizacaoEndereco": "Bloco A > Sala 101",
      "quantidadeDisponivel": 4,
      "quantidadeMinima": 2,
      "alertaReposicao": false,
      "criadoEm": "2026-11-26T11:00:00",
      "atualizadoEm": "2026-11-26T11:00:00"
    }
  ],
  "timestamp": "2026-11-26T12:00:00"
}
```

### Erros usados pelas consultas

As respostas abaixo ilustram o formato real de `ApiErrorResponse`. Datas e valores são fictícios. O handler não inclui stack trace, credenciais ou detalhes internos. `fieldErrors` pode aparecer quando houver erros de validação; as consultas listadas não recebem corpo validado.

`400 Bad Request` — exemplo ao enviar texto onde se espera um ID numérico, como `GET /api/v1/materiais/abc`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro da requisição inválido",
  "path": "/api/v1/materiais/abc"
}
```

`404 Not Found` — exemplo de ID numérico sem registro, como `GET /api/v1/materiais/999999`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Material não encontrado com ID: 999999",
  "path": "/api/v1/materiais/999999"
}
```

O `404` se aplica às buscas individuais que lançam `ResourceNotFoundException` e ao caminho de localização inexistente. Não se aplica a consultas de lista sem resultados, que retornam `200` e lista vazia.

## Situação e dependências

| Item | Situação na `develop` consultada | Acompanhamento |
|---|---|---|
| Consultas de localização | Integradas em `LocalizacaoController`. | Documento específico de GET localização está destacado em SCRUM-87; este índice registra o contrato integrador, sem duplicar a entrega detalhada. |
| Consultas de materiais | Integradas em `MaterialController` na `develop` consultada, incluindo lista, classificações, busca por ID/código e alerta de reposição. | O acompanhamento específico de GET materiais está destacado em SCRUM-88. A integração de SCRUM-6/22 deve ser acompanhada ao atualizar o contrato em futuras revisões. |
| Outros recursos e filtros ainda não presentes nos controllers | Planejados, não são rotas implementadas nesta versão. | Documentar quando os respectivos PRs/tarefas forem integrados. Não inferir endpoints futuros a partir dos documentos de planejamento. |
| Autenticação e proteção das rotas | Fora desta etapa; não foram adicionadas. | Melhoria futura vinculada a SCRUM-14/24/25 e às aulas 38–42 do UnityGO. A ausência de autenticação não torna as rotas adequadas para produção. |

Este documento registra o estado observado no código da branch `develop` e não altera branches de integração de outros integrantes.
