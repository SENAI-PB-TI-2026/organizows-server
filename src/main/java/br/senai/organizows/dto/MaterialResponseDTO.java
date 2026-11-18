package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Material;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponseDTO {

    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private CategoriaMaterial categoria;
    private String subgrupo;
    private String modelo;
    private String especificacoesTecnicas;
    private StatusMaterial status;
    private Long localId;
    private String localizacaoNome;
    private String localizacaoCodigo;
    private Integer quantidadeDisponivel;
    private Integer quantidadeMinima;
    private Boolean alertaReposicao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public MaterialResponseDTO(Material entity) {
        if (entity == null) {
            return;
        }
        this.id = entity.getId();
        this.codigo = entity.getCodigo();
        this.nome = entity.getNome();
        this.descricao = entity.getDescricao();
        this.categoria = entity.getCategoria();
        this.subgrupo = entity.getSubgrupo();
        this.modelo = entity.getModelo();
        this.especificacoesTecnicas = entity.getEspecificacoesTecnicas();
        this.status = entity.getStatus();
        this.localId = entity.getLocalId();
        if (entity.getLocalizacao() != null) {
            this.localizacaoNome = entity.getLocalizacao().getNome();
            this.localizacaoCodigo = entity.getLocalizacao().getCodigo();
        }
        this.quantidadeDisponivel = entity.getQuantidadeDisponivel();
        this.quantidadeMinima = entity.getQuantidadeMinima();
        this.alertaReposicao = entity.isAlertaReposicao();
        this.criadoEm = entity.getCriadoEm();
        this.atualizadoEm = entity.getAtualizadoEm();
    }
}