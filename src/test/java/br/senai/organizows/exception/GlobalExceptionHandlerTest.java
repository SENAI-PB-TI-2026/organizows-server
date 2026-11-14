package br.senai.organizows.exception;

import br.senai.organizows.controller.TechnicalStatusController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestExceptionController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test/exceptions")
    static class TestExceptionController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Recurso com ID 999 não foi encontrado");
        }

        @GetMapping("/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Parâmetro de consulta inválido");
        }

        @GetMapping("/general-error")
        public void throwGeneralError() {
            throw new RuntimeException("Falha inesperada no processamento: stack trace e detalhes internos confidenciais");
        }

        @GetMapping("/data-integrity")
        public void throwDataIntegrity() {
            throw new org.springframework.dao.DataIntegrityViolationException("Violação de FK interna do PostgreSQL/H2");
        }

        @PostMapping("/validation")
        public void throwValidation(@Valid @RequestBody TestRequestDTO dto) {
        }
    }

    @Data
    static class TestRequestDTO {
        @NotBlank(message = "O campo 'nome' é obrigatório")
        private String nome;
    }

    @Test
    @DisplayName("Deve formatar ResourceNotFoundException no padrão ApiErrorResponse com HTTP 404")
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/exceptions/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Recurso com ID 999 não foi encontrado"))
                .andExpect(jsonPath("$.path").value("/test/exceptions/not-found"))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }

    @Test
    @DisplayName("Deve formatar IllegalArgumentException no padrão ApiErrorResponse com HTTP 400")
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/exceptions/illegal-argument")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parâmetro de consulta inválido"))
                .andExpect(jsonPath("$.path").value("/test/exceptions/illegal-argument"))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }

    @Test
    @DisplayName("Deve formatar MethodArgumentNotValidException no padrão ApiErrorResponse com HTTP 400 e fieldErrors")
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(post("/test/exceptions/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Falha na validação dos campos da requisição"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("nome"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("O campo 'nome' é obrigatório"));
    }

    @Test
    @DisplayName("Deve formatar DataIntegrityViolationException no padrão ApiErrorResponse com HTTP 409")
    void shouldHandleDataIntegrityViolationException() throws Exception {
        mockMvc.perform(get("/test/exceptions/data-integrity")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A operação violou uma restrição de integridade de dados"));
    }

    @Test
    @DisplayName("Deve formatar erro genérico 500 sem vazamento de stacktrace ou detalhes internos")
    void shouldHandleGeneralExceptionWithoutDataLeakage() throws Exception {
        mockMvc.perform(get("/test/exceptions/general-error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado no servidor"))
                .andExpect(jsonPath("$.path").value("/test/exceptions/general-error"))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("confidenciais"))));
    }
}
