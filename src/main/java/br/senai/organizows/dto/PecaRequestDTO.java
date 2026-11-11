package br.senai.organizows.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PecaRequestDTO {

    @NotBlank(message = "O nome da peça é obrigatório")
    private String nome;

    private String codigo;

    private String categoria;

    private String descricao;

    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    private Integer quantidadeEstoque;

    @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;

    @DecimalMin(value = "0.0", inclusive = true, message = "O preço unitário não pode ser negativo")
    private BigDecimal precoUnitario;

    private String localizacao;
}
