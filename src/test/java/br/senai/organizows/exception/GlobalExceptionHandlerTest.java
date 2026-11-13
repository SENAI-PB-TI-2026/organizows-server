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
            throw new RuntimeException("Falha inesperada no processamento");
        }
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
    @DisplayName("Deve formatar erro genérico não tratado no padrão ApiErrorResponse com HTTP 500")
    void shouldHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/exceptions/general-error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado no servidor"))
                .andExpect(jsonPath("$.path").value("/test/exceptions/general-error"))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }
}
