package br.senai.organizows.dto.technical;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo de dados para retorno de verificação técnica do sistema")
public class TechnicalStatusDTO {

    @Schema(description = "Estado operacional do serviço backend", example = "UP")
    private String status;

    @Schema(description = "Identificador da aplicação", example = "organizows")
    private String applicationName;

    @Schema(description = "Versão atual da aplicação", example = "1.0.0")
    private String version;

    @Schema(description = "Perfil ou ambiente de execução atual", example = "development")
    private String environment;

    @Schema(description = "Data e hora do momento da verificação", example = "2026-11-12T21:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "Tempo total de atividade contínua do serviço (uptime)", example = "0 d, 00 h, 12 m, 34 s")
    private String uptime;
}
