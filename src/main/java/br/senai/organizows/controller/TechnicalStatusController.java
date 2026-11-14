package br.senai.organizows.controller;

import br.senai.organizows.dto.response.ApiErrorResponse;
import br.senai.organizows.dto.response.ApiResponse;
import br.senai.organizows.dto.technical.TechnicalStatusDTO;
import br.senai.organizows.dto.technical.TechnicalStatusApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Verificação Técnica", description = "Endpoints técnicos para conferência de conectividade, integridade e metadados operacionais do backend")
public class TechnicalStatusController {

    @Value("${spring.application.name:organizows}")
    private String applicationName;

    @Value("${application.version:1.0.0}")
    private String applicationVersion;

    @Value("${application.environment:development}")
    private String applicationEnvironment;

    @GetMapping
    @Operation(
            summary = "Verificar status técnico do backend",
            description = "Endpoint técnico de verificação que retorna a integridade da aplicação, tempo de atividade (uptime), versão e metadados operacionais para diagnóstico e testes de conectividade."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Aplicação em execução normal",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TechnicalStatusApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor ao processar verificação de status",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<TechnicalStatusDTO>> getStatus() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptimeMillis);
        String formattedUptime = String.format("%d d, %02d h, %02d m, %02d s",
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                duration.toSecondsPart());

        TechnicalStatusDTO status = TechnicalStatusDTO.builder()
                .status("UP")
                .applicationName(applicationName)
                .version(applicationVersion)
                .environment(applicationEnvironment)
                .timestamp(LocalDateTime.now())
                .uptime(formattedUptime)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Serviço operacional", status));
    }
}
