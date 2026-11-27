package br.senai.organizows.exception;

import br.senai.organizows.controller.LocalizacaoController;
import br.senai.organizows.service.LocalizacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

class GlobalExceptionHandlerExclusaoLocalizacaoTest {

    private static final Long LOCALIZACAO_ID = 1L;
    private static final String CAMINHO_EXCLUSAO = "/api/v1/localizacoes/" + LOCALIZACAO_ID;

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
    void deveMapearLocalizacaoComFilhosPara409() throws Exception {
        String mensagem = "Localização possui filhas e não pode ser excluída";
        doThrow(new LocalizacaoComFilhosException(mensagem)).when(localizacaoService).excluir(LOCALIZACAO_ID);

        MvcResult resultado = mockMvc.perform(delete(CAMINHO_EXCLUSAO)).andReturn();

        assertEquals(409, resultado.getResponse().getStatus());
        JsonNode corpo = jsonMapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertEquals(409, corpo.path("status").asInt());
        assertEquals(mensagem, corpo.path("message").asString());
        assertEquals(CAMINHO_EXCLUSAO, corpo.path("path").asString());
    }

    @Test
    void deveMapearLocalizacaoComMateriaisPara409() throws Exception {
        String mensagem = "Localização possui materiais associados e não pode ser excluída";
        doThrow(new LocalizacaoComMateriaisException(mensagem)).when(localizacaoService).excluir(LOCALIZACAO_ID);

        MvcResult resultado = mockMvc.perform(delete(CAMINHO_EXCLUSAO)).andReturn();

        assertEquals(409, resultado.getResponse().getStatus());
        JsonNode corpo = jsonMapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertEquals(409, corpo.path("status").asInt());
        assertEquals(mensagem, corpo.path("message").asString());
        assertEquals(CAMINHO_EXCLUSAO, corpo.path("path").asString());
    }

    @Test
    void devePreservarPadraoDeErroSemDadosSensiveis() throws Exception {
        String detalheInterno = "Falha fictícia ao acessar a tabela interna de localizações";
        doThrow(new IllegalStateException(detalheInterno)).when(localizacaoService).excluir(LOCALIZACAO_ID);

        MvcResult resultado = mockMvc.perform(delete(CAMINHO_EXCLUSAO)).andReturn();

        assertEquals(500, resultado.getResponse().getStatus());
        JsonNode corpo = jsonMapper.readTree(resultado.getResponse().getContentAsByteArray());
        assertEquals(500, corpo.path("status").asInt());
        assertEquals("Internal Server Error", corpo.path("error").asString());
        assertEquals("Ocorreu um erro interno inesperado no servidor", corpo.path("message").asString());
        assertEquals(CAMINHO_EXCLUSAO, corpo.path("path").asString());
        assertTrue(corpo.hasNonNull("timestamp"));
        assertEquals(5, corpo.size());
        assertFalse(corpo.toString().contains(detalheInterno));
    }
}
