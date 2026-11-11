package br.senai.organizows.dto;

import br.senai.organizows.model.Peca;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PecaResponseDTO {

    private Long id;
    private String nome;
    private String codigo;
    private String categoria;
    private String descricao;
    private Integer quantidadeEstoque;
    private Integer quantidadeMinima;
    private BigDecimal precoUnitario;
    private String localizacao;
    private boolean alertaEstoqueBaixo;

    public static PecaResponseDTO fromEntity(Peca peca) {
        if (peca == null) {
            return null;
        }

        int estoque = peca.getQuantidadeEstoque() != null ? peca.getQuantidadeEstoque() : 0;
        int minimo = peca.getQuantidadeMinima() != null ? peca.getQuantidadeMinima() : 0;

        return PecaResponseDTO.builder()
                .id(peca.getId())
                .nome(peca.getNome())
                .codigo(peca.getCodigo())
                .categoria(peca.getCategoria())
                .descricao(peca.getDescricao())
                .quantidadeEstoque(estoque)
                .quantidadeMinima(minimo)
                .precoUnitario(peca.getPrecoUnitario())
                .localizacao(peca.getLocalizacao())
                .alertaEstoqueBaixo(estoque <= minimo)
                .build();
    }
}
