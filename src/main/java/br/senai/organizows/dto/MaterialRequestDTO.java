package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDTO {

    @NotBlank(message = "A descrição do material é obrigatória")
    private String descricao;

    @NotNull(message = "A categoria do material é obrigatória")
    private CategoriaMaterial categoria;

    @NotNull(message = "O status do material é obrigatório")
    private StatusMaterial status;

    private Long localId;

    @NotNull(message = "A quantidade disponível é obrigatória")
    @Min(value = 0, message = "A quantidade disponível não pode ser negativa")
    private Integer quantidadeDisponivel;

    @NotNull(message = "A quantidade mínima é obrigatória")
    @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;
}