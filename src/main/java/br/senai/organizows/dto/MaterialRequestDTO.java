package br.senai.organizows.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Dados para cadastrar um material")
public class MaterialRequestDTO extends MaterialCadastroBaseDTO {

    @Schema(description = "Quantidade disponível no cadastro", example = "10")
    @NotNull(message = "A quantidade disponível é obrigatória")
    @Min(value = 0, message = "A quantidade disponível não pode ser negativa")
    private Integer quantidadeDisponivel;
}
