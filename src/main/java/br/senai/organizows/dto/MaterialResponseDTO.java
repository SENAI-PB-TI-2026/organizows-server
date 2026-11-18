package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Material;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MaterialResponseDTO {

    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private String caracteristicas;
    private String grupo;
    private String subgrupo;
    private CategoriaMaterial categoria;
    private StatusMaterial status;
    private Long localId;
    private Integer quantidadeDisponivel;
    private Integer quantidadeMinima;
    private Boolean alertaReposicao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public MaterialResponseDTO(Material entity) {
        this.id = entity.getId();
        this.codigo = entity.getCodigo();
        this.nome = entity.getNome();
        this.descricao = entity.getDescricao();
        this.caracteristicas = entity.getCaracteristicas();
        this.grupo = entity.getGrupo();
        this.subgrupo = entity.getSubgrupo();
        this.categoria = entity.getCategoria();
        this.status = entity.getStatus();
        this.localId = entity.getLocalId();
        this.quantidadeDisponivel = entity.getQuantidadeDisponivel();
        this.quantidadeMinima = entity.getQuantidadeMinima();
        this.alertaReposicao = entity.getQuantidadeDisponivel() <= entity.getQuantidadeMinima();
        this.criadoEm = entity.getCriadoEm();
        this.atualizadoEm = entity.getAtualizadoEm();
    }
}
