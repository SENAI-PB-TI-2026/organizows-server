---
name: flyway-data-integrity
description: Planejar migrations Flyway, entidades JPA, FKs, unicidade e validação do esquema no Organizows. Use sempre que alterar tabelas, relacionamentos ou configurações de banco.
---

# Persistência segura

1. Liste as migrations de `origin/develop` e confira versões já aplicadas; escolha a próxima versão livre no projeto. Não renomeie/edite migrations aplicadas nem duplique `V1`.
2. A entidade `Localizacao` e sua V1 são canônicas. Conecte `Material` e demais módulos por referência válida, sem tabela `local` concorrente.
3. Defina chaves, FKs, `NOT NULL`, `UNIQUE`, `CHECK`, defaults e índices; valide a regra no service e no banco. Considere dados históricos e upgrade de banco populado.
4. Preserve `ddl-auto=validate` e configuração por perfil. Nunca comite credenciais e não use `ddl-auto=update` para criar tabelas implicitamente.
5. Teste migration em banco vazio e cenário de atualização; execute teste de repository e integridade referencial. H2 não substitui teste PostgreSQL quando dialeto/tipos divergem.
6. Ao introduzir PostgreSQL, confirme JDBC driver e integração Flyway apropriados, e faça validação em container com versão fixada.
7. Registre compatibilidade H2/PostgreSQL, possíveis bloqueios e SQL exato criado, sem alegar execução que não ocorreu.

## Pronto quando
Migrações reproduzíveis, versões únicas, JPA validate aprovado e restrições comprovadas por testes.
