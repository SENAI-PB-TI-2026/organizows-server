package br.senai.organizows;

import br.senai.organizows.model.Localizacao;
import br.senai.organizows.repository.LocalizacaoRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class OrganizowsApplicationDevProfileTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Test
    @DisplayName("Deve inicializar aplicação com perfil dev e banco de desenvolvimento configurado")
    void deveInicializarComPerfilDev() throws SQLException {
        assertThat(dataSource).isNotNull();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            assertThat(metaData.getURL()).contains("organizows_dev");
        }
    }

    @Test
    @DisplayName("Deve aplicar migrações Flyway automaticamente no perfil dev sem intervenção manual")
    void deveAplicarMigracoesFlywayAutomaticamenteNoPerfilDev() throws SQLException {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, "PUBLIC", "LOCALIZACAO", null)) {
                assertThat(rs.next()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Deve persistir e consultar entidade no banco de desenvolvimento com migração aplicada")
    void devePersistirEConsultarNoBancoDev() {
        Localizacao loc = Localizacao.builder()
                .codigo("DEV-LOC-1")
                .nome("Área de Teste Dev")
                .tipo("Área")
                .build();

        Localizacao salvo = localizacaoRepository.save(loc);
        assertThat(salvo.getId()).isNotNull();
        assertThat(localizacaoRepository.findByCodigo("DEV-LOC-1")).isPresent();
    }
}
