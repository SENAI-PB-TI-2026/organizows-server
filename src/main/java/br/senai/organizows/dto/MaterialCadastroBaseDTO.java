package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class MaterialCadastroBaseDTO implements MaterialCadastroDTO {

    @NotBlank(message = "O código do material é obrigatório")
    @Size(max = 50)
    private String codigo;

    @NotBlank(message = "O nome do material é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "A descrição do material é obrigatória")
    @Size(max = 150)
    private String descricao;

    @Size(max = 2048)
    @URL(regexp = "(?i)https?://[^\\s]+", message = "Informe uma URL de imagem HTTP ou HTTPS válida")
    private String imagemUrl;

    @Size(max = 500)
    private String caracteristicas;

    @Size(max = 100)
    private String modelo;

    @Size(max = 500)
    private String especificacoesTecnicas;

    @NotBlank(message = "O grupo do material é obrigatório")
    @Size(max = 80)
    private String grupo;

    @Size(max = 80)
    private String subgrupo;

    @NotNull(message = "A categoria do material é obrigatória")
    private CategoriaMaterial categoria;

    @NotNull(message = "O status do material é obrigatório")
    private StatusMaterial status;

    @NotNull(message = "A localização do material é obrigatória")
    private Long localId;

    @NotNull(message = "A quantidade mínima é obrigatória")
    @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;
}
