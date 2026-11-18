# Integração técnica — SCRUM-6 / PR #4

O objetivo é trazer o cadastro de materiais do Weslley para a base `develop` sem modificar as entregas de infraestrutura existentes.

## O que mudou
- Preservados `pom.xml`, Java 25, Spring Boot 4.1.2, perfis H2/dev, tratamento global de exceções, V1 de localizações e instruções dos agentes existentes em `develop`.
- Excluídos do resultado integrado: a entidade paralela `Local`, a V1 duplicada, a exceção duplicada e alterações de projeto da IDE.
- `Material` referencia `Localizacao` existente, validada por `LocalizacaoRepository`; ID inválido resulta em 404 e FK é obrigatória.
- DTOs de Material usam identificador único `codigo`, `nome`, `descricao`, `caracteristicas`, `grupo`, `subgrupo`, categoria, status, localização e quantidades. `grupo` e `subgrupo` são campos simples nesta fase; não há catálogo hierárquico independente ou controle formal de grupos cadastrados.
- `/api/v1/materiais` usa o envelope `ApiResponse`; inclui CRUD, consulta por grupo, lista de reposição e `POST /api/v1/materiais/{id}/ajuste-saldo`.
- O ajuste transacional usa lock pessimista, recusa delta zero, saldo negativo e overflow. PUT cadastral não pode alterar o saldo diretamente.
- Migrations V2 e V3 usam tipos compatíveis com H2, constraints e FK com `localizacao`; V1 da develop permanece inalterada.

## Limitações e pendências
- **Este ajuste integra o código, mas não comprova todos os critérios SCRUM-6/21/22/44/45**: grupo/subgrupo ainda não são tabelas relacionadas, nem há validação de cadastro de grupos válidos.
- Não há histórico de movimentações de estoque; pertence a tarefas posteriores de movimentação e auditoria.
- Autenticação/autorização não é implementada: conteúdo ainda não ministrado, API não está pronta para exposição pública.
- Não foram executados `./mvnw clean verify` nem testes automatizados nesta correção por indisponibilidade de JDK 25/dependências no ambiente de edição. Execute localmente com JDK 25, perfil H2 e Swagger/Postman antes de aprovar o PR.
- As migrations V2/V3 existentes na branch estudantil **foram revisadas antes da integração**. Se um desenvolvedor aplicou versões anteriores dessas migrations em banco persistente, não deve sobrescrever checksum do Flyway ou usar `repair` sem avaliar os dados; para H2 temporário, recrie a base de teste. Em banco com dados, planeje migração corretiva específica.

## Verificação manual sugerida
1. Iniciar a API com perfil `h2` e confirmar `GET /api/v1/status`.
2. Criar localização via `POST /api/v1/localizacoes`.
3. Criar material com `localId` existente e todos os campos obrigatórios; conferir status 201.
4. Listar materiais e consultar por ID/grupo; conferir `ApiResponse.data`.
5. Tentar localização inexistente (404), código duplicado (409), saldo negativo (400) e grupo/nomes vazios (400).
6. Ajustar saldo de 100 para 90 com `{"deltaQuantidade":-10}`, recusar retirada superior ao disponível e delta zero.
7. Conferir alerta de reposição antes/depois da alteração de quantidade.

**Não realizar merge com develop antes da conferência e aprovação da equipe.**
