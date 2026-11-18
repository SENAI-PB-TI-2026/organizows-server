package br.senai.organizows.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjusteSaldoRequestDTO {
    @NotNull(message = "O ajuste de saldo é obrigatório")
    private Integer deltaQuantidade;
}
