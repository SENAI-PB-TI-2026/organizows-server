package br.senai.organizows.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AjusteSaldoRequestDTO {

    @NotNull(message = "O ajuste de saldo é obrigatório")
    @JsonAlias("delta")
    private Integer deltaQuantidade;
}
