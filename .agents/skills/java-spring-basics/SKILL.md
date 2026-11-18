---
name: java-spring-basics
description: Orientar programação clara e consistente em Java e Spring Boot para alunos do Organizows. Use ao criar classes, métodos, DTOs, controllers e services ou ao refatorar código.
---

# Programar com clareza

## Antes de implementar
- Leia exemplos equivalentes em `src/main/java/br/senai/organizows/`. Siga padrões já ensinados e usados; não crie abstrações por antecipação.
- Explique o caminho da requisição: `Controller -> Service -> Repository -> Banco`.
- Implemente somente o que os critérios da tarefa pedem. Não acrescente bibliotecas ou altere arquitetura sem necessidade.

## Organização
- Uma classe com responsabilidade principal; pacotes existentes `model`, `repository`, `service`, `controller`, `dto`.
- Classes em `PascalCase`, métodos/variáveis em `camelCase`, constantes em `UPPER_SNAKE_CASE`.
- Nomes indicam propósito: `buscarPorId`, `listarMateriais`, `validarQuantidade`. Evite `a`, `x`, `aux`, `teste1` no código definitivo.
- Prefira métodos curtos, sem código duplicado, sem comentários que repetem a linha; comente *o motivo* de regras menos óbvias.
- Não retorne entidade JPA diretamente quando o projeto já prevê `ResponseDTO`; use o modelo de `ApiResponse` existente.
- O Controller só recebe/encaminha requisições, o Service executa a regra, e o Repository persiste.
- Valide campos necessários: string vazia, identificador inexistente, quantidade negativa e vínculo inválido quando aplicáveis.
- Não use `catch (Exception)` genérico para esconder erro. Reaproveite tratamento de erros existente.
- Respeite `Localizacao`, Flyway e estrutura da branch `develop`; não crie tabela/entidade duplicada.

## Verificação adequada ao aprendizado
Descreva um roteiro manual simples, por exemplo: iniciar a aplicação, cadastrar pelo Swagger/Postman, consultar o registro, tentar entrada inválida e comparar resposta. Quando não for possível executar, deixe claro. Testes automatizados são evolução futura e **não são exigidos nesta fase**.

## Saída
Resumo do que foi alterado, explicação acessível das decisões, arquivos modificados, como testar manualmente e itens pendentes.

## Segurança e conteúdo ainda não ministrado
- Para as tarefas atuais, **não gere** classes de login, autenticação, autorização, JWT, Spring Security, perfis de usuário ou controle de acesso. Isso não faz parte das práticas já ensinadas.
- Quando encontrar um requisito futuro de perfil/aluno/professor, indique o cartão e o bloqueio pedagógico, sem inventar uma solução ou remover o requisito.
- Não exponha o protótipo como serviço de produção nem descreva rotas de escrita sem autenticação como seguras. Continue aplicando validações de campos e boas práticas básicas.
