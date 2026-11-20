package br.senai.organizows.repository;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class MaterialRepositoryTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    @Autowired
    private EntityManager entityManager;

    private Localizacao localizacaoSalva;

    @BeforeEach
    void setUp() {
        materialRepository.deleteAll();
        Localizacao loc = Localizacao.builder()
                .codigo("ALM-01")
                .nome("Almoxarifado Principal")
                .tipo("ALMOXARIFADO")
                .build();
        localizacaoSalva = localizacaoRepository.save(loc);
    }

    @Test
    @DisplayName("Deve persistir e buscar material com localização associada com sucesso")
    void devePersistirMaterialComLocalizacao() {
        Material mat = Material.builder()
                .codigo("MAT-TEST-1")
                .nome("Conector RJ45")
                .descricao("Conector de rede Cat6")
                .grupo("Conectividade")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Conectores")
                .modelo("RJ45 Macho")
                .especificacoesTecnicas("Banhado a ouro")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(50)
                .quantidadeMinima(10)
                .localizacao(localizacaoSalva)
                .build();

        Material salvo = materialRepository.save(mat);
        entityManager.flush();
        entityManager.clear();

        Optional<Material> encontrado = materialRepository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("MAT-TEST-1", encontrado.get().getCodigo());
        assertEquals("Conector RJ45", encontrado.get().getNome());
        assertNotNull(encontrado.get().getLocalizacao());
        assertEquals(localizacaoSalva.getId(), encontrado.get().getLocalizacao().getId());
    }

    @Test
    @DisplayName("Deve rejeitar persistência de materiais com código duplicado via UK constraint")
    void deveRejeitarCodigoDuplicado() {
        Material mat1 = Material.builder()
                .codigo("MAT-DUP")
                .nome("Material 1")
                .descricao("Test material 1")
                .grupo("Ferramentas")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(5)
                .quantidadeMinima(2)
                .localizacao(localizacaoSalva)
                .build();
        materialRepository.save(mat1);
        entityManager.flush();

        Material mat2 = Material.builder()
                .codigo("MAT-DUP")
                .nome("Material 2")
                .descricao("Test material 2")
                .grupo("Ferramentas")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(10)
                .quantidadeMinima(3)
                .localizacao(localizacaoSalva)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            materialRepository.save(mat2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Deve rejeitar persistÃªncia quando descriÃ§Ã£o obrigatÃ³ria estiver ausente")
    void deveRejeitarDescricaoNula() {
        Material materialSemDescricao = Material.builder()
                .codigo("MAT-SEM-DESCRICAO")
                .nome("Material sem descrição")
                .grupo("Testes")
                .categoria(CategoriaMaterial.COMPONENTE_ELETRONICO)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(0)
                .quantidadeMinima(0)
                .localizacao(localizacaoSalva)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            materialRepository.save(materialSemDescricao);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Deve buscar materiais para reposição onde saldo <= estoque mínimo")
    void deveBuscarMateriaisParaReposicao() {
        // mat1: 3 <= 5 -> alerta (menor que mínimo)
        Material mat1 = Material.builder()
                .codigo("MAT-REP-1")
                .nome("Parafuso M3")
                .descricao("Parafuso")
                .grupo("Fixadores")
                .categoria(CategoriaMaterial.CONSUMIVEL)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(3)
                .quantidadeMinima(5)
                .localizacao(localizacaoSalva)
                .build();

        // mat2: 5 <= 5 -> alerta (limiar exato)
        Material mat2 = Material.builder()
                .codigo("MAT-REP-2")
                .nome("Fita Isolante")
                .descricao("Fita isolante")
                .grupo("Elétrica")
                .categoria(CategoriaMaterial.CONSUMIVEL)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(5)
                .quantidadeMinima(5)
                .localizacao(localizacaoSalva)
                .build();

        // mat3: 10 > 5 -> NÃO deve entrar no alerta
        Material mat3 = Material.builder()
                .codigo("MAT-REP-3")
                .nome("Alicate")
                .descricao("Alicate")
                .grupo("Ferramentas")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(10)
                .quantidadeMinima(5)
                .localizacao(localizacaoSalva)
                .build();

        materialRepository.saveAll(List.of(mat1, mat2, mat3));
        entityManager.flush();

        List<Material> reposicao = materialRepository.findMateriaisParaReposicao();
        assertEquals(1, reposicao.size());
        assertTrue(reposicao.stream().anyMatch(m -> m.getCodigo().equals("MAT-REP-1")));
        assertFalse(reposicao.stream().anyMatch(m -> m.getCodigo().equals("MAT-REP-2")));
        assertFalse(reposicao.stream().anyMatch(m -> m.getCodigo().equals("MAT-REP-3")));
    }

    @Test
    @DisplayName("Deve filtrar materiais por categoria, status, código e nome")
    void deveFiltrarMateriais() {
        Material mat = Material.builder()
                .codigo("MAT-FILTRO")
                .nome("Multímetro Digital")
                .descricao("Multímetro")
                .grupo("Instrumentos")
                .categoria(CategoriaMaterial.FERRAMENTA)
                .status(StatusMaterial.EM_MANUTENCAO)
                .quantidadeDisponivel(2)
                .quantidadeMinima(1)
                .localizacao(localizacaoSalva)
                .build();
        materialRepository.save(mat);
        entityManager.flush();

        assertTrue(materialRepository.findByCodigo("MAT-FILTRO").isPresent());
        assertTrue(materialRepository.existsByCodigo("MAT-FILTRO"));
        assertFalse(materialRepository.existsByCodigo("MAT-INEXISTENTE"));

        List<Material> porCategoria = materialRepository.findByCategoria(CategoriaMaterial.FERRAMENTA);
        assertFalse(porCategoria.isEmpty());

        List<Material> porStatus = materialRepository.findByStatus(StatusMaterial.EM_MANUTENCAO);
        assertFalse(porStatus.isEmpty());

        List<Material> porNome = materialRepository.findByNomeContainingIgnoreCase("multímetro");
        assertFalse(porNome.isEmpty());

        List<Material> porLocal = materialRepository.findByLocalizacaoId(localizacaoSalva.getId());
        assertFalse(porLocal.isEmpty());
    }
}
