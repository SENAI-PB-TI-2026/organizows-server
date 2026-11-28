package br.senai.organizows.controller;

import br.senai.organizows.dto.localizacao.ImpedimentosExclusaoResponseDTO;
import br.senai.organizows.exception.GlobalExceptionHandler;
import br.senai.organizows.exception.ResourceNotFoundException;
import br.senai.organizows.service.LocalizacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class LocalizacaoControllerImpedimentosExclusaoTest {

    private static final Long LOCALIZACAO_ID = 42L;
    private static final String CAMINHO_IMPEDIMENTOS =
            "/api/v1/localizacoes/" + LOCALIZACAO_ID + "/impedimentos-exclusao";

    private final LocalizacaoService localizacaoService = mock(LocalizacaoService.class);
    private final JsonMapper jsonMapper = new JsonMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void configurarControllerComTratamentoDeErros() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LocalizacaoController(localizacaoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar200ComIndicadorEMotivos() throws Exception {
        List<String> motivos = List.of("POSSUI_LOCALIZACOES_FILHAS", "POSSUI_MATERIAIS_ASSOCIADOS");
        when(localizacaoService.consultarImpedimentosExclusao(LOCALIZACAO_ID))
                .thenReturn(new ImpedimentosExclusaoResponseDTO(false, motivos));

        MvcResult resultado = mockMvc.perform(get(CAMINHO_IMPEDIMENTOS)).andReturn();

        assertEquals(200, resultado.getResponse().getStatus());
        JsonNode corpo = jsonMapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertTrue(corpo.path("success").asBoolean());
        assertFalse(corpo.path("data").path("podeExcluir").asBoolean());
        assertEquals(2, corpo.path("data").path("motivos").size());
        assertEquals(motivos.get(0), corpo.path("data").path("motivos").get(0).asString());
        assertEquals(motivos.get(1), corpo.path("data").path("motivos").get(1).asString());
        verify(localizacaoService).consultarImpedimentosExclusao(LOCALIZACAO_ID);
        verifyNoMoreInteractions(localizacaoService);
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        String mensagem = "Localização não encontrada com ID: " + LOCALIZACAO_ID;
        doThrow(new ResourceNotFoundException(mensagem))
                .when(localizacaoService).consultarImpedimentosExclusao(LOCALIZACAO_ID);

        MvcResult resultado = mockMvc.perform(get(CAMINHO_IMPEDIMENTOS)).andReturn();

        assertEquals(404, resultado.getResponse().getStatus());
        JsonNode corpo = jsonMapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertEquals(404, corpo.path("status").asInt());
        assertEquals(mensagem, corpo.path("message").asString());
        assertEquals(CAMINHO_IMPEDIMENTOS, corpo.path("path").asString());
        verify(localizacaoService).consultarImpedimentosExclusao(LOCALIZACAO_ID);
        verifyNoMoreInteractions(localizacaoService);
    }
}
