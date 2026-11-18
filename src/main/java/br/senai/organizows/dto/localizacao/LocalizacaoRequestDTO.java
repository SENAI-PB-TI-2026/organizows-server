package br.senai.organizows.dto.localizacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de requisição para cadastro e atualização de localização")
public class LocalizacaoRequestDTO {

    @NotBlank(message = "O código é obrigatório")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres")
    @Schema(description = "Código identificador único da localização", example = "BLOCO-A")
    private String codigo;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
    @Schema(description = "Nome descritivo da localização", example = "Bloco Administrativo")
    private String nome;

    @Size(max = 100, message = "O tipo não pode exceder 100 caracteres")
    @Schema(description = "Tipo ou classificação do espaço (ex: Prédio, Galpão, Sala, Prateleira)", example = "Prédio")
    private String tipo;

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres")
    @Schema(description = "Descrição detalhada da localização", example = "Prédio principal do campus")
    private String descricao;

    @Schema(description = "ID da localização pai (opcional, para compor hierarquia física)", example = "1")
    private Long localPaiId;
}
