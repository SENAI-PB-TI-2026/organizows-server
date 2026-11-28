package br.senai.organizows.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class GlobalExceptionHandlerParametroTest {

    private static final String MENSAGEM_PADRONIZADA = "Parâmetro da requisição inválido";

    private final JsonMapper jsonMapper = new JsonMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void configurarControllerDeTeste() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveMapearParametroComTipoInvalidoPara400() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/teste/parametros/nao-numerico")).andReturn();

        validarRespostaDeParametroInvalido(resultado, "/teste/parametros/nao-numerico");
    }

    @Test
    void deveMapearParametroObrigatorioAusentePara400() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/teste/parametro-obrigatorio")).andReturn();

        validarRespostaDeParametroInvalido(resultado, "/teste/parametro-obrigatorio");
    }

    private void validarRespostaDeParametroInvalido(MvcResult resultado, String caminho) throws Exception {
        assertEquals(400, resultado.getResponse().getStatus());
        String corpoJson = resultado.getResponse().getContentAsString();
        JsonNode corpo = jsonMapper.readTree(corpoJson);

        assertEquals(400, corpo.path("status").asInt());
        assertEquals("Bad Request", corpo.path("error").asString());
        assertEquals(MENSAGEM_PADRONIZADA, corpo.path("message").asString());
        assertEquals(caminho, corpo.path("path").asString());
        assertFalse(corpoJson.contains("MethodArgumentTypeMismatchException"));
        assertFalse(corpoJson.contains("MissingServletRequestParameterException"));
        assertFalse(corpoJson.contains("stackTrace"));
        assertTrue(corpo.path("timestamp").isString());
    }

    @RestController
    @RequestMapping("/teste")
    static class ControllerDeTeste {

        @GetMapping("/parametros/{id}")
        String buscarPorId(@PathVariable Long id) {
            return id.toString();
        }

        @GetMapping("/parametro-obrigatorio")
        String buscarPorParametro(@RequestParam String categoria) {
            return categoria;
        }
    }
}
