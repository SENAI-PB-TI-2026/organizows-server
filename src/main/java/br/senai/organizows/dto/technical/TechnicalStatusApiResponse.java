package br.senai.organizows.dto.technical;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Esquema explícito da resposta HTTP do endpoint técnico; espelha
 * ApiResponse<TechnicalStatusDTO> sem modificar o contrato de execução.
 */
@Schema(name = "TechnicalStatusApiResponse", description = "Envelope da resposta de status técnico")
public class TechnicalStatusApiResponse {
    @Schema(description = "Indica sucesso", example = "true")
    public boolean success;
    @Schema(description = "Mensagem da operação", example = "Serviço operacional")
    public String message;
    @Schema(description = "Informações operacionais")
    public TechnicalStatusDTO data;
    @Schema(description = "Data e hora da resposta")
    public LocalDateTime timestamp;
}
