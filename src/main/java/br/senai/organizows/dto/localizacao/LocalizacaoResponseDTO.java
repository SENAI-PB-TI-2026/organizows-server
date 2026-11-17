package br.senai.organizows.dto.localizacao;

import br.senai.organizows.model.Localizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta representando uma localização com dados hierárquicos")
public class LocalizacaoResponseDTO {

    @Schema(description = "Identificador único da localização", example = "1")
    private Long id;

    @Schema(description = "Código único da localização", example = "BLOCO-A")
    private String codigo;

    @Schema(description = "Nome da localização", example = "Bloco Administrativo")
    private String nome;

    @Schema(description = "Tipo ou classificação da localização", example = "Prédio")
    private String tipo;

    @Schema(description = "Descrição detalhada da localização", example = "Prédio principal do campus")
    private String descricao;

    @Schema(description = "Identificador da localização pai, se houver", example = "null")
    private Long localPaiId;

    @Schema(description = "Nome da localização pai, se houver", example = "null")
    private String localPaiNome;

    @Schema(description = "Endereço físico completo formatado na hierarquia", example = "Bloco Administrativo > Sala 101")
    private String enderecoCompleto;

    public static LocalizacaoResponseDTO fromEntity(Localizacao localizacao) {
        if (localizacao == null) {
            return null;
        }

        Long paiId = null;
        String paiNome = null;
        if (localizacao.getLocalPai() != null) {
            paiId = localizacao.getLocalPai().getId();
            paiNome = localizacao.getLocalPai().getNome();
        }

        return LocalizacaoResponseDTO.builder()
                .id(localizacao.getId())
                .codigo(localizacao.getCodigo())
                .nome(localizacao.getNome())
                .tipo(localizacao.getTipo())
                .descricao(localizacao.getDescricao())
                .localPaiId(paiId)
                .localPaiNome(paiNome)
                .enderecoCompleto(localizacao.getEnderecoCompleto())
                .build();
    }
}
