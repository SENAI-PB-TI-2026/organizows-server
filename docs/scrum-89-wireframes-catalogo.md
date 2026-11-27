# SCRUM-89 — Wireframes do catálogo

Proposta de interface para consultar materiais, ver seus detalhes e orientar a pessoa quando a consulta não encontra resultados. Os desenhos são wireframes textuais: servem para discutir estrutura e conteúdo, sem exigir React ou definir cores finais.

## Referência funcional

Na `develop` consultada, `GET /api/v1/materiais` lista os materiais e aceita o filtro opcional `grupo`. Uma lista sem correspondências retorna `200` com `data: []`. `GET /api/v1/materiais/{id}` e `GET /api/v1/materiais/codigo/{codigo}` consultam um item; item inexistente retorna `404`. A resposta do material contém código, nome, descrição, grupo, subgrupo, categoria, status, localização, quantidades e indicador de reposição, entre outros campos.

O contrato atual não oferece filtro por nome ou código na listagem, paginação nem rota de interface. Por isso, os desenhos mostram o filtro por grupo como disponível. Busca por texto, ordenação e paginação ficam como ideias para validar e dependem de suporte futuro na API.

## Consulta — desktop

```text
+----------------------------------------------------------------------------------+
| Organizows                                      Catálogo de materiais             |
+----------------------------------------------------------------------------------+
| Materiais                                                                           |
| Consulte os itens e veja sua disponibilidade.                                      |
|                                                                                    |
| Grupo [Todos os grupos                              v] [Aplicar filtro]             |
|                                                                                    |
| Código       Nome                 Grupo          Status          Qtd.   Local       |
| MAT-EXEMPLO  Chave de exemplo     Ferramentas    Disponível       4     Sala 101   |
| ...                                                                                |
|                                                                                    |
| Selecione uma linha para abrir os detalhes do material.                            |
+----------------------------------------------------------------------------------+
```

## Consulta — celular

```text
+----------------------------------+
| Organizows                       |
| Catálogo de materiais            |
|                                  |
| Grupo                            |
| [Todos os grupos              v] |
| [Aplicar filtro]                 |
|                                  |
| MAT-EXEMPLO                      |
| Chave de exemplo                 |
| Ferramentas · Disponível         |
| Quantidade: 4                    |
| Local: Sala 101                  |
| [Ver detalhes]                   |
|                                  |
| ...                              |
+----------------------------------+
```

Em telas estreitas, cada resultado vira um cartão para evitar tabela espremida. O filtro ocupa a largura disponível.

## Detalhe — desktop

```text
+----------------------------------------------------------------------------------+
| Organizows                                      Catálogo de materiais             |
+----------------------------------------------------------------------------------+
| [← Voltar ao catálogo]                                                            |
| Chave de exemplo                                             Disponível            |
| Código: MAT-EXEMPLO                                                                |
|                                                                                    |
| Descrição                                                                          |
| Item fictício para documentação                                                   |
|                                                                                    |
| CLASSIFICAÇÃO                  ESTOQUE                    LOCALIZAÇÃO               |
| Grupo: Ferramentas             Disponível: 4              Sala 101                 |
| Subgrupo: Manuais              Mínimo: 2                  Bloco A > Sala 101       |
| Categoria: Ferramenta          [Alerta de reposição se aplicável]                  |
+----------------------------------------------------------------------------------+
```

## Detalhe — celular

```text
+----------------------------------+
| [← Voltar]                       |
| Chave de exemplo                 |
| MAT-EXEMPLO · Disponível         |
|                                  |
| Descrição                        |
| Item fictício para documentação  |
|                                  |
| Classificação                    |
| Ferramentas / Manuais            |
| Categoria: Ferramenta            |
|                                  |
| Estoque                          |
| Disponível: 4 · Mínimo: 2        |
|                                  |
| Localização                      |
| Sala 101                         |
| Bloco A > Sala 101               |
|                                  |
| [Alerta de reposição se aplicável]|
+----------------------------------+
```

## Estado sem resultados — desktop

```text
+----------------------------------------------------------------------------------+
| Organizows                                      Catálogo de materiais             |
+----------------------------------------------------------------------------------+
| Materiais                                                                           |
| Grupo [Grupo selecionado                            v] [Aplicar filtro]             |
|                                                                                    |
|                              Nenhum material encontrado                            |
|                      Tente outro grupo ou limpe o filtro aplicado.                 |
|                              [Limpar filtro]                                       |
+----------------------------------------------------------------------------------+
```

## Estado sem resultados — celular

```text
+----------------------------------+
| [← Catálogo]                     |
| Catálogo de materiais            |
| Grupo                            |
| [Grupo selecionado            v] |
| [Aplicar filtro]                 |
|                                  |
| Nenhum material encontrado       |
| Tente outro grupo ou limpe       |
| o filtro aplicado.               |
| [Limpar filtro]                  |
+----------------------------------+
```

Este estado corresponde à resposta bem-sucedida da listagem com `data: []`; não deve ser exibido como falha de servidor.

## Componentes e mensagens

| Elemento | Comportamento proposto |
|---|---|
| Cabeçalho e título | Identificam Organizows e a área de catálogo. |
| Seletor de grupo | Usa as classificações disponíveis; a consulta atual aceita `grupo` em `GET /api/v1/materiais`. |
| Botão aplicar/limpar filtro | Atualiza a listagem ou volta à consulta sem filtro. |
| Linha ou cartão de material | Resume código, nome, grupo, status, quantidade e localização. |
| Ação “Ver detalhes” | Abre a consulta individual; o layout deve mostrar carregamento enquanto aguarda. |
| Detalhes do material | Organizam descrição, classificação, estoque e localização em seções. Modelo e especificações podem ser apresentados quando preenchidos. |
| Estado vazio | Informa “Nenhum material encontrado” e oferece limpar ou trocar o filtro. |
| Estado de erro | Apresenta uma mensagem simples, preserva a opção de tentar novamente e não exibe detalhes técnicos do servidor. |

Mensagens sugeridas para erros da API:

| Situação | Mensagem na interface |
|---|---|
| Falha de conexão ou `500` ao listar | “Não foi possível carregar os materiais. Tente novamente.” |
| `400` por parâmetro inválido | “Não foi possível fazer a consulta. Confira os dados e tente novamente.” |
| `404` ao abrir um detalhe | “Material não encontrado. Volte ao catálogo e escolha outro item.” |

As mensagens da interface são amigáveis; em desenvolvimento, a equipe pode consultar o `status` e o `path` retornados pela API para investigar a falha. Uma resposta `200` com lista vazia permanece no estado vazio, sem mensagem de erro.

## Conferência dos critérios e validação

| Critério | Registro | Como conferir | Situação |
|---|---|---|---|
| Wireframes de consulta, detalhe e sem resultados para desktop e celular | Seções acima | Comparar cada par de desenhos e observar a adaptação de tabela para cartões e o empilhamento dos detalhes | Registrado; wireframes não executados em navegador |
| Componentes e mensagens de erro sem exigir React | Tabelas “Componentes e mensagens” | Revisar os elementos, os estados HTTP e as mensagens propostas | Registrado |
| Alinhamento aos requisitos do Organizows e arquivos no repositório | Este documento e contrato descrito em `docs/consultas-publicas-catalogo.md` | Comparar campos e respostas com `MaterialController`, `MaterialResponseDTO` e `GlobalExceptionHandler` | Conferência documental realizada; validação com responsável/colega ainda pendente |

Este documento especifica a interface e não altera endpoints nem afirma que a proposta foi aprovada por revisão externa.
