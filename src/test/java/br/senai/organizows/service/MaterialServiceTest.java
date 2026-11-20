package br.senai.organizows.service;

import br.senai.organizows.dto.MaterialRequestDTO;
import br.senai.organizows.dto.MaterialResponseDTO;
import br.senai.organizows.dto.MaterialUpdateRequestDTO;
import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.exception.CodigoMaterialDuplicadoException;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.model.Localizacao;
import br.senai.organizows.model.Material;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private LocalizacaoRepository localizacaoRepository;

    @InjectMocks
    private MaterialService materialService;

    private Localizacao localizacao;
    private Material material;
    private MaterialRequestDTO requestDTO;
    private MaterialUpdateRequestDTO updateRequestDTO;

    @BeforeEach
    void setUp() {
        localizacao = Localizacao.builder()
                .id(1L)
                .codigo("LOC-A")
                .nome("Prateleira A")
                .build();

        material = Material.builder()
                .id(10L)
                .codigo("MAT-100")
                .nome("Cabo de Rede")
                .descricao("Cabo Azul Cat6")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Rede")
                .modelo("Cat6")
                .especificacoesTecnicas("305m")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(20)
                .quantidadeMinima(10)
                .localizacao(localizacao)
                .build();

        requestDTO = MaterialRequestDTO.builder()
                .codigo("MAT-100")
                .nome("Cabo de Rede")
                .descricao("Cabo Azul Cat6")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Rede")
                .modelo("Cat6")
                .especificacoesTecnicas("305m")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeDisponivel(20)
                .quantidadeMinima(10)
                .localId(1L)
                .build();

        updateRequestDTO = MaterialUpdateRequestDTO.builder()
                .codigo("MAT-100")
                .nome("Cabo de Rede")
                .descricao("Cabo Azul Cat6")
                .grupo("Cabos")
                .categoria(CategoriaMaterial.CABO)
                .subgrupo("Rede")
                .modelo("Cat6")
                .especificacoesTecnicas("305m")
                .status(StatusMaterial.DISPONIVEL)
                .quantidadeMinima(10)
                .localId(1L)
                .build();
    }

    @Test
    @DisplayName("Deve criar material com localização existente com sucesso")
    void deveCriarMaterialComSucesso() {
        when(materialRepository.existsByCodigo("MAT-100")).thenReturn(false);
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material m = invocation.getArgument(0);
            m.setId(10L);
            return m;
        });

        MaterialResponseDTO response = materialService.criar(requestDTO);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("MAT-100", response.getCodigo());
        assertEquals(1L, response.getLocalId());
        assertEquals("Prateleira A", response.getLocalizacaoNome());
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    @DisplayName("Deve rejeitar criação com código duplicado lançando CodigoMaterialDuplicadoException")
    void deveRejeitarCodigoDuplicadoAoCriar() {
        when(materialRepository.existsByCodigo("MAT-100")).thenReturn(true);

        CodigoMaterialDuplicadoException ex = assertThrows(CodigoMaterialDuplicadoException.class,
                () -> materialService.criar(requestDTO));

        assertTrue(ex.getMessage().contains("Já existe um material cadastrado com o código"));
        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    @DisplayName("Deve rejeitar criação quando localId inexistente e NÃO persistir o material")
    void deveRejeitarLocalIdInexistenteAoCriar() {
        when(materialRepository.existsByCodigo("MAT-100")).thenReturn(false);
        when(localizacaoRepository.findById(999L)).thenReturn(Optional.empty());

        requestDTO.setLocalId(999L);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> materialService.criar(requestDTO));

        assertTrue(ex.getMessage().contains("Localização não encontrada com ID: 999"));
        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    @DisplayName("Deve rejeitar criação com localId nulo")
    void deveRejeitarCriacaoComLocalIdNulo() {
        requestDTO.setLocalId(null);
        assertThrows(IllegalArgumentException.class, () -> materialService.criar(requestDTO));
        verify(localizacaoRepository, never()).findById(any());
        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    @DisplayName("Deve rejeitar quantidades negativas na criação")
    void deveRejeitarQuantidadesNegativasAoCriar() {
        requestDTO.setQuantidadeDisponivel(-1);
        assertThrows(IllegalArgumentException.class, () -> materialService.criar(requestDTO));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar criaÃ§Ã£o sem descriÃ§Ã£o antes de persistir")
    void deveRejeitarCriacaoSemDescricao() {
        when(materialRepository.existsByCodigo("MAT-100")).thenReturn(false);
        requestDTO.setDescricao(null);

        assertThrows(IllegalArgumentException.class, () -> materialService.criar(requestDTO));

        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));

        MaterialResponseDTO response = materialService.buscarPorId(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void deveLancarExceptionAoBuscarIdInexistente() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> materialService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve atualizar material e preservar o saldo atual contra desvios de fluxo")
    void deveAtualizarMaterialEPreservarSaldo() {
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));
        when(localizacaoRepository.findById(1L)).thenReturn(Optional.of(localizacao));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateRequestDTO.setNome("Cabo de Rede Alterado");

        MaterialResponseDTO response = materialService.atualizar(10L, updateRequestDTO);

        assertNotNull(response);
        assertEquals("Cabo de Rede Alterado", response.getNome());
        assertEquals(20, response.getQuantidadeDisponivel(), "O saldo disponível deve ser preservado e não sobrescrito pelo PUT");
    }

    @Test
    @DisplayName("Deve deletar material existente com sucesso")
    void deveDeletarMaterialComSucesso() {
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));

        materialService.deletar(10L);

        verify(materialRepository).delete(material);
    }

    @Test
    @DisplayName("Deve rejeitar ajuste de saldo com delta nulo ou zero")
    void deveRejeitarAjusteSaldoComDeltaNuloOuZero() {
        assertThrows(IllegalArgumentException.class, () -> materialService.ajustarSaldo(10L, null));
        assertThrows(IllegalArgumentException.class, () -> materialService.ajustarSaldo(10L, 0));
        verify(materialRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("Deve impedir saldo final negativo no ajuste de saldo")
    void deveImpedirSaldoFinalNegativo() {
        material.setQuantidadeDisponivel(5);
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> materialService.ajustarSaldo(10L, -10));

        assertTrue(ex.getMessage().contains("não pode ficar negativo"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve prevenir overflow numérico no ajuste de saldo com Math.addExact")
    void devePrevenirOverflowNumericoNoAjuste() {
        material.setQuantidadeDisponivel(100);
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> materialService.ajustarSaldo(10L, Integer.MAX_VALUE));

        assertTrue(ex.getMessage().contains("overflow"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve ajustar saldo positivo com sucesso e usar bloqueio pessimista")
    void deveAjustarSaldoPositivoComSucesso() {
        material.setQuantidadeDisponivel(10);
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        MaterialResponseDTO response = materialService.ajustarSaldo(10L, 5);

        assertEquals(15, response.getQuantidadeDisponivel());
        verify(materialRepository).findByIdForUpdate(10L);
        verify(materialRepository).save(material);
    }

    @Test
    @DisplayName("Deve ajustar saldo negativo quando há estoque suficiente")
    void deveAjustarSaldoNegativoComSucesso() {
        material.setQuantidadeDisponivel(10);
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArgument(0));

        MaterialResponseDTO response = materialService.ajustarSaldo(10L, -4);

        assertEquals(6, response.getQuantidadeDisponivel());
    }

    @Test
    @DisplayName("Deve verificar limiares de alerta de reposição: menor, igual e maior que mínimo")
    void deveVerificarLimiaresAlertaReposicao() {
        material.setQuantidadeMinima(10);

        // Caso 1: menor que o mínimo (9 < 10) -> true
        material.setQuantidadeDisponivel(8);
        when(materialRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(material));
        when(materialRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        MaterialResponseDTO r1 = materialService.ajustarSaldo(10L, 1); // vira 9
        assertTrue(r1.getAlertaReposicao(), "Alerta deve ser ativado para saldo < minimo");

        // Caso 2: igual ao mínimo (10 == 10) -> true (SCRUM-12 limiar exato)
        material.setQuantidadeDisponivel(9);
        MaterialResponseDTO r2 = materialService.ajustarSaldo(10L, 1); // vira 10
        assertFalse(r2.getAlertaReposicao(), "SCRUM-12 não alerta no limiar exato saldo == mínimo");

        // Caso 3: maior que o mínimo (11 > 10) -> false
        material.setQuantidadeDisponivel(10);
        MaterialResponseDTO r3 = materialService.ajustarSaldo(10L, 1); // vira 11
        assertFalse(r3.getAlertaReposicao(), "Alerta deve ser desativado para saldo > minimo");
    }
}
