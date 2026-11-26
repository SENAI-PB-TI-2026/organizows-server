# SCRUM-22 — Validação da persistência de materiais

[Cartão SCRUM-22](https://team-senai.atlassian.net/browse/SCRUM-22)

A entidade `Material`, o `MaterialRepository` e as migrações V2 e V3 já estavam integrados na `develop`. A validação foi feita sobre a base `f8b7043`, com Java 25 e H2. Nenhuma migração foi alterada.

| Critério | Verificação e resultado |
| --- | --- |
| Código e descrição persistidos | O banco retornou o código `M22` e a descrição `Chave de teste` após a inserção. Um código duplicado foi rejeitado com SQLSTATE `23505`. |
| Quantidades não negativas | Saldo disponível e quantidade mínima negativos foram rejeitados com SQLSTATE `23513`. |
| Localização existente | O material foi gravado com uma localização cadastrada. Uma referência inexistente foi rejeitada com SQLSTATE `23506`. |
| Banco limpo | O Flyway aplicou V1 a V4 em um H2 vazio. Os seis testes existentes de migração e repositório passaram. |

Para repetir os testes existentes no Windows, com Java 25:

```powershell
.\mvnw.cmd '-Dtest=FlywayMigrationEmptyDatabaseTest,MaterialRepositoryTest' test
```

Para conferir as restrições manualmente em um banco descartável, aplique as migrações, insira uma localização e um material válido e leia a linha persistida. Repita a inserção com código duplicado, saldo negativo, mínimo negativo e localização inexistente, alterando um campo por vez.
