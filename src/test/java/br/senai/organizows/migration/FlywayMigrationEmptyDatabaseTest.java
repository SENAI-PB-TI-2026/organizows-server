package br.senai.organizows.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlywayMigrationEmptyDatabaseTest {

    @Test
    @DisplayName("Deve aplicar migração Flyway V1 com sucesso em um banco de dados totalmente vazio")
    void deveAplicarMigracaoFlywayEmBancoVazio() throws SQLException {
        // Criar um banco H2 exclusivo e vazio
        String dbName = "empty_db_" + UUID.randomUUID().toString().replace("-", "");
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Verificar que o banco está vazio antes da migração
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "LOCALIZACAO", null)) {
                assertThat(rs.next()).isFalse();
            }
        }

        // Executar Flyway Migration
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        MigrateResult result = flyway.migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(1);
        assertThat(result.targetSchemaVersion).isEqualTo("1");

        // Validar que a tabela e todas as colunas foram criadas corretamente
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Validar existência da tabela LOCALIZACAO
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "LOCALIZACAO", null)) {
                assertThat(rs.next()).isTrue();
            }

            // Validar colunas esperadas
            Set<String> colunasEncontradas = new HashSet<>();
            try (ResultSet rs = metaData.getColumns(null, "PUBLIC", "LOCALIZACAO", null)) {
                while (rs.next()) {
                    colunasEncontradas.add(rs.getString("COLUMN_NAME").toUpperCase());
                }
            }

            assertThat(colunasEncontradas).contains(
                    "ID",
                    "CODIGO",
                    "NOME",
                    "TIPO",
                    "DESCRICAO",
                    "LOCAL_PAI_ID"
            );

            // Inserir registro raiz com sucesso
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (codigo, nome, tipo, descricao) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, "GALPAO-01");
                stmt.setString(2, "Galpão Central");
                stmt.setString(3, "Galpão");
                stmt.setString(4, "Área de estocagem principal");
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Inserir registro filho com chave estrangeira
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (codigo, nome, tipo, local_pai_id) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, "CORR-01");
                stmt.setString(2, "Corredor 1");
                stmt.setString(3, "Corredor");
                stmt.setLong(4, 1L);
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Validar constraint única de código no banco
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (codigo, nome) VALUES (?, ?)")) {
                stmt.setString(1, "GALPAO-01"); // código duplicado
                stmt.setString(2, "Galpão Duplicado");
                assertThatThrownBy(stmt::executeUpdate)
                        .isInstanceOf(SQLException.class);
            }

            // Validar existência do índice para local_pai_id
            boolean indexEncontrado = false;
            try (ResultSet rs = metaData.getIndexInfo(null, "PUBLIC", "LOCALIZACAO", false, false)) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    if ("IDX_LOCALIZACAO_LOCAL_PAI_ID".equalsIgnoreCase(indexName)) {
                        indexEncontrado = true;
                        break;
                    }
                }
            }
            assertThat(indexEncontrado).isTrue();

            // Validar constraint CHECK que impede local_pai_id igual a id
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (id, codigo, nome, local_pai_id) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, 99L);
                stmt.setString(2, "CHK-AUTOREF");
                stmt.setString(3, "Autorreferência Direta");
                stmt.setLong(4, 99L);
                assertThatThrownBy(stmt::executeUpdate)
                        .isInstanceOf(SQLException.class);
            }
        }
    }
}
