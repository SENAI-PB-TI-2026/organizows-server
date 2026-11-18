package br.senai.organizows.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhes de erro em campo específico de validação")
public class FieldErrorDetail {

    @Schema(description = "Nome do campo com erro", example = "nome")
    private String field;

    @Schema(description = "Mensagem descritiva do erro no campo", example = "O campo não pode estar em branco")
    private String message;
}
