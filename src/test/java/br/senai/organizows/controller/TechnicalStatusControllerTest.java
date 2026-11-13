package br.senai.organizows.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TechnicalStatusController.class)
class TechnicalStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar status 200 OK e informações técnicas do sistema")
    void shouldReturnTechnicalStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Serviço operacional"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.applicationName").value("organizows"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.environment").value("development"))
                .andExpect(jsonPath("$.data.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.data.uptime").value(notNullValue()))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }
}
