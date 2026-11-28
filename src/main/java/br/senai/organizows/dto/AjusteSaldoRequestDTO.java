package br.senai.organizows.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variação a aplicar ao saldo de um material")
public class AjusteSaldoRequestDTO {

    @Schema(description = "Delta inteiro aplicado ao saldo atual; não pode ser zero", example = "5")
    @NotNull(message = "O ajuste de saldo é obrigatório")
    @JsonAlias("delta")
    private Integer deltaQuantidade;
}
