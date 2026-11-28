package br.senai.organizows.service;

import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.repository.LocalizacaoRepository;
import br.senai.organizows.repository.MaterialRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LocalizacaoServiceConsultarImpedimentosExclusaoTest {

    private static final Long LOCALIZACAO_ID = 42L;

    private final LocalizacaoRepository localizacaoRepository = mock(LocalizacaoRepository.class);
    private final MaterialRepository materialRepository = mock(MaterialRepository.class);
    private final LocalizacaoService localizacaoService =
            new LocalizacaoService(localizacaoRepository, materialRepository);

    @Test
    void deveRejeitarLocalizacaoInexistente() {
        when(localizacaoRepository.existsById(LOCALIZACAO_ID)).thenReturn(false);

        ResourceNotFoundException excecao = assertThrows(
                ResourceNotFoundException.class,
                () -> localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID)
        );

        assertEquals("Localização não encontrada com ID: " + LOCALIZACAO_ID, excecao.getMessage());
        verify(localizacaoRepository).existsById(LOCALIZACAO_ID);
        verifyNoMoreInteractions(localizacaoRepository, materialRepository);
    }
}
