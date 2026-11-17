---
name: spring-api-patterns
description: Criar ou revisar controllers, services, DTOs, repositories e tratamento de erros Spring Boot no Organizows. Use para CRUD, contratos HTTP, validação e integração REST.
---

# Contrato REST do Organizows

- Confirme a versão de Spring Boot no `pom.xml`; não a troque.
- Controllers em `/api/v1/` devem delegar regras ao service e usar DTOs, nunca expor entidades JPA.
- Valide RequestDTO com `@Valid`, `@NotBlank`, `@NotNull`, `@Size`, `@Min` conforme domínio e coluna.
- Use `ApiResponse<T>` nos sucessos e o `GlobalExceptionHandler` / `ApiErrorResponse` existentes nos erros. Recursos ausentes: 404; validações: 400; conflitos: 409; falhas inesperadas: 500. Não capture `RuntimeException` genericamente para retornar 404.
- Mantenha transformação de entidades e leitura de relacionamentos lazy dentro de transações apropriadas.
- Respeite `CorsConfig`: nunca `@CrossOrigin(origins = "*")` sem requisito e aprovação explícitos.
- Considere idempotência, paginação/filtros, índices e semântica 200/201/204 para cada endpoint.
- Produza testes de controller para sucessos, campos inválidos, inexistentes, conflito e resposta sem referências circulares.

## Critérios de pronto
Rotas documentadas no OpenAPI/README quando aplicável; validação em DTO e banco; erro previsível; testes com saída real e compatibilidade com `develop`.
