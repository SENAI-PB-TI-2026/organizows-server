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
    @DisplayName("Deve aplicar migrações Flyway V1 e V2 com sucesso em um banco de dados totalmente vazio")
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
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "MATERIAL", null)) {
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
        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(2);
        assertThat(result.targetSchemaVersion).isEqualTo("2");

        // Validar que as tabelas e todas as colunas foram criadas corretamente
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Validar existência da tabela LOCALIZACAO
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "LOCALIZACAO", null)) {
                assertThat(rs.next()).isTrue();
            }

            // Validar colunas esperadas de LOCALIZACAO
            Set<String> colunasLocalizacao = new HashSet<>();
            try (ResultSet rs = metaData.getColumns(null, "PUBLIC", "LOCALIZACAO", null)) {
                while (rs.next()) {
                    colunasLocalizacao.add(rs.getString("COLUMN_NAME").toUpperCase());
                }
            }
            assertThat(colunasLocalizacao).contains("ID", "CODIGO", "NOME", "TIPO", "DESCRICAO", "LOCAL_PAI_ID");

            // Validar existência da tabela MATERIAL
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "MATERIAL", null)) {
                assertThat(rs.next()).isTrue();
            }

            // Validar colunas esperadas de MATERIAL
            Set<String> colunasMaterial = new HashSet<>();
            try (ResultSet rs = metaData.getColumns(null, "PUBLIC", "MATERIAL", null)) {
                while (rs.next()) {
                    colunasMaterial.add(rs.getString("COLUMN_NAME").toUpperCase());
                }
            }
            assertThat(colunasMaterial).contains(
                    "ID", "CODIGO", "NOME", "DESCRICAO", "CATEGORIA", "SUBGRUPO",
                    "MODELO", "ESPECIFICACOES_TECNICAS", "STATUS", "QUANTIDADE_DISPONIVEL",
                    "QUANTIDADE_MINIMA", "LOCALIZACAO_ID", "CRIADO_EM", "ATUALIZADO_EM"
            );

            // Inserir registro raiz em localizacao com sucesso
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (codigo, nome, tipo, descricao) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, "GALPAO-01");
                stmt.setString(2, "Galpão Central");
                stmt.setString(3, "Galpão");
                stmt.setString(4, "Área de estocagem principal");
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Inserir registro filho em localizacao com chave estrangeira
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO localizacao (codigo, nome, tipo, local_pai_id) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, "CORR-01");
                stmt.setString(2, "Corredor 1");
                stmt.setString(3, "Corredor");
                stmt.setLong(4, 1L);
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Inserir material com sucesso associado a localização existente
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, localizacao_id, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-FLY-1");
                stmt.setString(2, "Cabo UTP Cat6");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, 50);
                stmt.setInt(6, 10);
                stmt.setLong(7, 1L);
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Inserir material com localizacao_id nula (opcional) com sucesso
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, localizacao_id, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-FLY-2");
                stmt.setString(2, "Conector RJ45");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, 100);
                stmt.setInt(6, 20);
                int rows = stmt.executeUpdate();
                assertThat(rows).isEqualTo(1);
            }

            // Validar constraint única de código no banco (Material)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-FLY-1"); // código duplicado
                stmt.setString(2, "Outro Cabo");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, 10);
                stmt.setInt(6, 5);
                assertThatThrownBy(stmt::executeUpdate).isInstanceOf(SQLException.class);
            }

            // Validar FK inválida em material (rejeição de localizacao_id inexistente)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, localizacao_id, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-FK-ERR");
                stmt.setString(2, "Material Inexistente");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, 10);
                stmt.setInt(6, 5);
                stmt.setLong(7, 99999L);
                assertThatThrownBy(stmt::executeUpdate).isInstanceOf(SQLException.class);
            }

            // Validar constraint CHECK de quantidade disponível negativa
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-NEG-1");
                stmt.setString(2, "Material Negativo");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, -1);
                stmt.setInt(6, 5);
                assertThatThrownBy(stmt::executeUpdate).isInstanceOf(SQLException.class);
            }

            // Validar constraint CHECK de quantidade mínima negativa
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO material (codigo, nome, categoria, status, quantidade_disponivel, quantidade_minima, criado_em, atualizado_em) " +
                            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, "MAT-NEG-2");
                stmt.setString(2, "Material Negativo Min");
                stmt.setString(3, "CABO");
                stmt.setString(4, "DISPONIVEL");
                stmt.setInt(5, 10);
                stmt.setInt(6, -5);
                assertThatThrownBy(stmt::executeUpdate).isInstanceOf(SQLException.class);
            }
        }
    }
}
