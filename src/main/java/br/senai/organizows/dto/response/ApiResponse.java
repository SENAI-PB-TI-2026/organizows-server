package br.senai.organizows.dto.response;

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
@Schema(description = "Envelope padronizado para respostas de sucesso da API")
public class ApiResponse<T> {

    @Schema(description = "Indica se a operação foi bem-sucedida", example = "true")
    private boolean success;

    @Schema(description = "Mensagem descritiva da operação", example = "Operação realizada com sucesso")
    private String message;

    @Schema(description = "Dados de retorno da operação")
    private T data;

    @Schema(description = "Data e hora em que a resposta foi gerada", example = "2026-11-12T21:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operação realizada com sucesso")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
