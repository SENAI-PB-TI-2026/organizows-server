package br.senai.organizows.dto;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import br.senai.organizows.model.Material;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponseDTO {

    private Long id;
    private String descricao;
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
        this.descricao = entity.getDescricao();
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