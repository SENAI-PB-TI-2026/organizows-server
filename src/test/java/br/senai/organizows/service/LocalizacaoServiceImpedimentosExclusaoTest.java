package br.senai.organizows.service;

import br.senai.organizows.dto.localizacao.ImpedimentosExclusaoResponseDTO;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LocalizacaoServiceImpedimentosExclusaoTest {

    private static final Long LOCALIZACAO_ID = 1L;
    private static final String MOTIVO_FILHAS = "POSSUI_LOCALIZACOES_FILHAS";
    private static final String MOTIVO_MATERIAIS = "POSSUI_MATERIAIS_ASSOCIADOS";

    private final LocalizacaoRepository localizacaoRepository = mock(LocalizacaoRepository.class);
    private final MaterialRepository materialRepository = mock(MaterialRepository.class);
    private final LocalizacaoService localizacaoService =
            new LocalizacaoService(localizacaoRepository, materialRepository);

    @BeforeEach
    void simularLocalizacaoExistente() {
        when(localizacaoRepository.existsById(LOCALIZACAO_ID)).thenReturn(true);
    }

    @Test
    void devePermitirQuandoNaoHaVinculos() {
        when(localizacaoRepository.existsByLocalPaiId(LOCALIZACAO_ID)).thenReturn(false);
        when(materialRepository.existsByLocalizacaoId(LOCALIZACAO_ID)).thenReturn(false);

        ImpedimentosExclusaoResponseDTO resultado =
                localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID);

        assertTrue(resultado.podeExcluir());
        assertTrue(resultado.motivos().isEmpty());
        verificarConsultasSemGravacao();
    }

    @Test
    void deveImpedirComFilhas() {
        when(localizacaoRepository.existsByLocalPaiId(LOCALIZACAO_ID)).thenReturn(true);
        when(materialRepository.existsByLocalizacaoId(LOCALIZACAO_ID)).thenReturn(false);

        ImpedimentosExclusaoResponseDTO resultado =
                localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID);

        assertFalse(resultado.podeExcluir());
        assertEquals(List.of(MOTIVO_FILHAS), resultado.motivos());
        verificarConsultasSemGravacao();
    }

    @Test
    void deveImpedirComMateriaisMesmoSaldoZero() {
        when(localizacaoRepository.existsByLocalPaiId(LOCALIZACAO_ID)).thenReturn(false);
        // Mesmo com saldo zero, a consulta de existência retorna true para um material vinculado.
        when(materialRepository.existsByLocalizacaoId(LOCALIZACAO_ID)).thenReturn(true);

        ImpedimentosExclusaoResponseDTO resultado =
                localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID);

        assertFalse(resultado.podeExcluir());
        assertEquals(List.of(MOTIVO_MATERIAIS), resultado.motivos());
        verificarConsultasSemGravacao();
    }

    @Test
    void deveRetornarAmbosMotivos() {
        when(localizacaoRepository.existsByLocalPaiId(LOCALIZACAO_ID)).thenReturn(true);
        when(materialRepository.existsByLocalizacaoId(LOCALIZACAO_ID)).thenReturn(true);

        ImpedimentosExclusaoResponseDTO resultado =
                localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID);

        assertFalse(resultado.podeExcluir());
        assertEquals(2, resultado.motivos().size());
        assertTrue(resultado.motivos().containsAll(List.of(MOTIVO_FILHAS, MOTIVO_MATERIAIS)));
        verificarConsultasSemGravacao();
    }

    private void verificarConsultasSemGravacao() {
        verify(localizacaoRepository).existsById(LOCALIZACAO_ID);
        verify(localizacaoRepository).existsByLocalPaiId(LOCALIZACAO_ID);
        verify(materialRepository).existsByLocalizacaoId(LOCALIZACAO_ID);
        verifyNoMoreInteractions(localizacaoRepository, materialRepository);
    }
}
