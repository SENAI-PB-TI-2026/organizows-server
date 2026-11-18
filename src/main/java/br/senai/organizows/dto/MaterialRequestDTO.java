package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestDTO {

    @NotBlank(message = "O código do material é obrigatório")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    private String codigo;

    @NotBlank(message = "O nome do material é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "A categoria do material é obrigatória")
    private CategoriaMaterial categoria;

    @Size(max = 100, message = "O subgrupo deve ter no máximo 100 caracteres")
    private String subgrupo;

    @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
    private String modelo;

    @Size(max = 500, message = "As especificações técnicas devem ter no máximo 500 caracteres")
    private String especificacoesTecnicas;

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