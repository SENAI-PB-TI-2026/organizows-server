package br.senai.organizows.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoqueDTO {

    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantidade; // Positivo para entrada, negativo para saída

    private String motivo; // Ex: Compra de lote, Uso em protótipo, Descarte por queima
}
