package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Campos comuns usados para cadastrar ou atualizar um material")
public abstract class MaterialCadastroBaseDTO implements MaterialCadastroDTO {

    @Schema(description = "Código único do material", example = "MAT-EXEMPLO-001")
    @NotBlank(message = "O código do material é obrigatório")
    @Size(max = 50)
    private String codigo;

    @Schema(description = "Nome do material", example = "Teclado USB")
    @NotBlank(message = "O nome do material é obrigatório")
    @Size(max = 150)
    private String nome;

    @Schema(description = "Descrição do material", example = "Teclado USB padrão ABNT2")
    @NotBlank(message = "A descrição do material é obrigatória")
    @Size(max = 150)
    private String descricao;

    @Schema(description = "Características do material", example = "Conexão USB e layout ABNT2")
    @Size(max = 500)
    private String caracteristicas;

    @Schema(description = "Modelo do material", example = "KB-100")
    @Size(max = 100)
    private String modelo;

    @Schema(description = "Especificações técnicas", example = "Conexão USB 2.0")
    @Size(max = 500)
    private String especificacoesTecnicas;

    @Schema(description = "Grupo da classificação", example = "Periféricos")
    @NotBlank(message = "O grupo do material é obrigatório")
    @Size(max = 80)
    private String grupo;

    @Schema(description = "Subgrupo da classificação", example = "Entrada")
    @Size(max = 80)
    private String subgrupo;

    @Schema(description = "Categoria do material", example = "PERIFERICO")
    @NotNull(message = "A categoria do material é obrigatória")
    private CategoriaMaterial categoria;

    @Schema(description = "Estado atual do material", example = "DISPONIVEL")
    @NotNull(message = "O status do material é obrigatório")
    private StatusMaterial status;

    @Schema(description = "ID de uma localização já cadastrada", example = "1")
    @NotNull(message = "A localização do material é obrigatória")
    private Long localId;

    @Schema(description = "Quantidade abaixo da qual o material entra em alerta de reposição", example = "5")
    @NotNull(message = "A quantidade mínima é obrigatória")
    @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
    private Integer quantidadeMinima;
}
