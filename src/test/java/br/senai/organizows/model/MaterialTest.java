package br.senai.organizows.model;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {

    @Test
    @DisplayName("Deve calcular alerta de reposição corretamente: true quando menor que o mínimo")
    void deveAtivarAlertaQuandoMenorQueMinimo() {
        Material material = Material.builder()
                .quantidadeDisponivel(3)
                .quantidadeMinima(5)
                .build();

        assertTrue(material.isAlertaReposicao());
    }

    @Test
    @DisplayName("Deve calcular alerta de reposição corretamente: true no limiar exato (igual ao mínimo)")
    void deveAtivarAlertaNoLimiarExato() {
        Material material = Material.builder()
                .quantidadeDisponivel(5)
                .quantidadeMinima(5)
                .build();

        assertTrue(material.isAlertaReposicao(), "No limiar quantidadeDisponivel == quantidadeMinima o alerta deve ser ativado conforme SCRUM-12");
    }

    @Test
    @DisplayName("Deve calcular alerta de reposição corretamente: false quando estritamente maior que o mínimo")
    void deveDesativarAlertaQuandoMaiorQueMinimo() {
        Material material = Material.builder()
                .quantidadeDisponivel(6)
                .quantidadeMinima(5)
                .build();

        assertFalse(material.isAlertaReposicao());
    }

    @Test
    @DisplayName("Deve inicializar timestamps e valores padrão em prePersist")
    void deveInicializarCamposEmPrePersist() {
        Material material = Material.builder()
                .codigo("MAT-001")
                .nome("Cabo de Rede")
                .categoria(CategoriaMaterial.CABO)
                .build();

        material.prePersist();

        assertNotNull(material.getCriadoEm());
        assertNotNull(material.getAtualizadoEm());
        assertEquals(StatusMaterial.DISPONIVEL, material.getStatus());
        assertEquals(0, material.getQuantidadeDisponivel());
        assertEquals(0, material.getQuantidadeMinima());
    }

    @Test
    @DisplayName("Deve retornar localId corretamente através da entidade vinculada Localizacao")
    void deveRetornarLocalIdDeFormaSegura() {
        Material materialSemLocal = new Material();
        assertNull(materialSemLocal.getLocalId());

        Localizacao loc = Localizacao.builder().id(99L).codigo("BANC-1").nome("Bancada 1").build();
        Material materialComLocal = Material.builder().localizacao(loc).build();
        assertEquals(99L, materialComLocal.getLocalId());
    }
}
