package br.senai.organizows.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AjusteSaldoRequestDTO {

    @NotNull(message = "A quantidade de ajuste (delta) é obrigatória")
    private Integer delta;
    @NotNull(message = "O ajuste de saldo é obrigatório")
    private Integer deltaQuantidade;
}
