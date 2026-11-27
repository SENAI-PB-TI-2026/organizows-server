# SCRUM-87 — Consultas GET de localizações

Esta página descreve as rotas GET de localização existentes na branch `develop`, conforme `LocalizacaoController`, `LocalizacaoResponseDTO` e o tratamento de erros da API. As respostas de sucesso usam o envelope `ApiResponse<T>`: `success`, `message`, `data` e `timestamp` (data e hora ISO-8601).

## Rotas disponíveis

| Rota | Parâmetros | Campos de `data` | Respostas previstas |
|---|---|---|---|
| `GET /api/v1/localizacoes` | Query opcional `codigo` (texto). Quando preenchido, procura por código exato e retorna lista de zero ou um elemento. | Lista com `id`, `codigo`, `nome`, `tipo`, `descricao`, `localPaiId`, `localPaiNome`, `enderecoCompleto` | `200`; sem correspondência, `data` é uma lista vazia. |
| `GET /api/v1/localizacoes/raizes` | Nenhum | Lista com os campos de localização acima | `200`, inclusive quando a lista estiver vazia. |
| `GET /api/v1/localizacoes/{id}` | Path `id` (número inteiro longo) | Objeto com os campos de localização acima | `200` se encontrado; `400` se o `id` não for numérico; `404` se não existir. |
| `GET /api/v1/localizacoes/codigo/{codigo}` | Path `codigo` (texto) | Objeto com os campos de localização acima | `200` se encontrado; `404` se não existir. |
| `GET /api/v1/localizacoes/{id}/filhas` | Path `id` (número inteiro longo) | Lista com os campos de localização acima | `200`; sem filhas, ou para ID sem registros associados, a consulta retorna lista vazia. `400` se o `id` não for numérico. |
| `GET /api/v1/localizacoes/{id}/caminho` | Path `id` (número inteiro longo) | Lista ordenada da raiz até a localização, com os campos acima | `200` se encontrado; `400` se o `id` não for numérico; `404` se não existir. |
| `GET /api/v1/localizacoes/{id}/impedimentos-exclusao` | Path `id` (número inteiro longo) | Objeto com `podeExcluir` (booleano) e `motivos` (lista) | `200` se a localização existe; `400` se o `id` não for numérico; `404` se não existir. |

## Exemplos

Os valores abaixo são fictícios e servem apenas para mostrar o formato das respostas.

### Sucesso com filtro por código

Requisição: `GET /api/v1/localizacoes?codigo=SALA-101`

Resposta `200 OK`:

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

### Consulta sem correspondência

Requisição: `GET /api/v1/localizacoes?codigo=CODIGO-INEXISTENTE`

Resposta `200 OK`:

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": [],
  "timestamp": "2026-11-26T12:00:00"
}
```

### Erro de parâmetro

Requisição: `GET /api/v1/localizacoes/abc` — o `id` deve ser numérico.

Resposta `400 Bad Request`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro da requisição inválido",
  "path": "/api/v1/localizacoes/abc"
}
```

### Localização não encontrada

Requisição: `GET /api/v1/localizacoes/999999`

Resposta `404 Not Found`:

```json
{
  "timestamp": "2026-11-26T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Localização não encontrada com ID: 999999",
  "path": "/api/v1/localizacoes/999999"
}
```

### Consultar impedimentos de exclusão

Requisição: `GET /api/v1/localizacoes/12/impedimentos-exclusao`

Resposta `200 OK` (exemplo fictício):

```json
{
  "success": true,
  "message": "Operação realizada com sucesso",
  "data": {
    "podeExcluir": false,
    "motivos": ["POSSUI_MATERIAIS_ASSOCIADOS"]
  },
  "timestamp": "2026-11-26T12:00:00"
}
```

O endpoint também retorna `404 Not Found` quando a localização não existe. `motivos` pode conter `POSSUI_LOCALIZACOES_FILHAS`, `POSSUI_MATERIAIS_ASSOCIADOS`, ambos ou nenhum, conforme as relações encontradas no momento da consulta.

O corpo de erro apresenta somente dados fictícios e campos do `ApiErrorResponse`; não inclui stack trace, credenciais nem detalhes internos. O `404` vale para buscas individuais por ID/código e para caminho hierárquico sem localização. Listas sem resultados retornam `200` e lista vazia.

## Estado e limites

As rotas listadas estão implementadas em `LocalizacaoController` na `develop` consultada. `/{id}/impedimentos-exclusao` foi integrado depois da primeira versão deste documento (SCRUM-93) e foi acrescentado nesta revisão integradora SCRUM-81 para manter os documentos alinhados. Consultas GET não alteram dados. Este documento não implementa autenticação ou autorização; a ausência desses mecanismos não torna as rotas adequadas para produção.

## Verificação

Documento conferido contra as rotas do controller, os campos de `LocalizacaoResponseDTO`, o envelope `ApiResponse` e o `GlobalExceptionHandler`. A aplicação e os exemplos HTTP não foram executados nesta entrega.
