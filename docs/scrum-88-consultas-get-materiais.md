# SCRUM-88 — Consultas GET de materiais

Esta página descreve as consultas GET implementadas em `MaterialController` na branch `develop` consultada. As respostas de sucesso usam `ApiResponse<T>`, com os campos `success`, `message`, `data` e `timestamp` (data e hora ISO-8601). Os exemplos usam valores fictícios.

## Rotas disponíveis

| Rota | Parâmetros | Campos principais de `data` | Respostas previstas |
|---|---|---|---|
| `GET /api/v1/materiais` | Query opcional `grupo`. Ausente ou vazio lista todos; preenchido filtra por grupo, ignorando maiúsculas/minúsculas. | Lista com `id`, `codigo`, `nome`, `descricao`, `caracteristicas`, `modelo`, `especificacoesTecnicas`, `grupo`, `subgrupo`, `categoria`, `status`, `localId`, `localizacaoNome`, `localizacaoEndereco`, `quantidadeDisponivel`, `quantidadeMinima`, `alertaReposicao`, `criadoEm`, `atualizadoEm` | `200`; se não houver correspondências, retorna `data: []`. |
| `GET /api/v1/materiais/classificacoes` | Nenhum | Lista com `categoria`, `grupo`, `subgrupos` | `200`, retorna as classificações definidas, mesmo sem materiais cadastrados. |
| `GET /api/v1/materiais/{id}` | Path `id` (número inteiro longo) | Um material com os campos listados acima | `200` se encontrado; `400` se o `id` não for numérico; `404` se não existir. |
| `GET /api/v1/materiais/codigo/{codigo}` | Path `codigo` (texto) | Um material com os campos listados acima | `200` se encontrado; `404` se não existir. |
| `GET /api/v1/materiais/alerta-reposicao` | Nenhum | Lista de materiais com os campos listados acima | `200`; sem alertas, retorna `data: []`. |

Este documento registra as rotas existentes no controller. Na `develop` consultada, as cinco rotas GET da tabela estão integradas; nenhuma delas está apenas planejada. Rotas que apareçam somente em outra branch ou em um PR não são tratadas como disponíveis até serem integradas em `develop`.

## Exemplos

### Lista filtrada com material

Requisição: `GET /api/v1/materiais?grupo=Ferramentas`

Resposta `200 OK`:

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

### Lista sem correspondências

Requisição: `GET /api/v1/materiais?grupo=GRUPO-INEXISTENTE`

Resposta `200 OK`:

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": [],
  "timestamp": "2026-11-26T12:00:00"
}
```

### Classificações disponíveis

Requisição: `GET /api/v1/materiais/classificacoes`

Trecho ilustrativo de `data` na resposta `200 OK`:

```json
[
  {
    "categoria": "FERRAMENTA",
    "grupo": "Ferramentas",
    "subgrupos": ["Manuais", "Elétricas"]
  }
]
```

### Erros de busca individual

As respostas de erro usam `ApiErrorResponse` (`timestamp`, `status`, `error`, `message`, `path` e, quando aplicável, `fieldErrors`). Os exemplos são fictícios e não expõem stack trace, credenciais ou informações internas.

ID não numérico, por exemplo `GET /api/v1/materiais/abc` — resposta `400 Bad Request`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro da requisição inválido",
  "path": "/api/v1/materiais/abc"
}
```

Material inexistente, por exemplo `GET /api/v1/materiais/999999` — resposta `404 Not Found`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Material não encontrado com ID: 999999",
  "path": "/api/v1/materiais/999999"
}
```

O `404` também se aplica à busca individual por código sem correspondência. Listas vazias continuam retornando `200`.

## Situação e limites

As consultas acima já existem em `MaterialController` na `develop` consultada, acompanhando a integração de materiais SCRUM-6/22 presente nessa base. Assim, `GET /api/v1/materiais` não é apenas planejado nesta documentação. Consultas GET não alteram materiais. Nenhum mecanismo de autenticação ou autorização foi implementado aqui; a ausência deles não torna as rotas adequadas para produção.

## Verificação

Documento comparado com as anotações `@GetMapping` do controller, `MaterialResponseDTO`, `ClassificacaoMaterialResponseDTO`, `ApiResponse` e `GlobalExceptionHandler`. A aplicação e os exemplos HTTP não foram executados nesta entrega.
