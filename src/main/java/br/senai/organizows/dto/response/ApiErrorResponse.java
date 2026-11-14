package br.senai.organizows.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estrutura padronizada de resposta de erro da API")
public class ApiErrorResponse {

    @Schema(description = "Data e hora do momento do erro", example = "2026-11-12T21:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Código de status HTTP correspondente", example = "400")
    private int status;

    @Schema(description = "Descrição sucinta do erro HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem explicativa e amigável sobre o erro ocorrido", example = "Parâmetros inválidos na requisição")
    private String message;

    @Schema(description = "URI da requisição que gerou o erro", example = "/api/v1/status")
    private String path;

    @Schema(description = "Lista detalhada de erros em campos específicos, quando houver falha de validação")
    private List<FieldErrorDetail> fieldErrors;
}
