package br.senai.organizows.model;

import br.senai.organizows.enums.CategoriaMaterial;
import br.senai.organizows.enums.StatusMaterial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoriaMaterial categoria;

    @Column(length = 100)
    private String subgrupo;

    @Column(length = 100)
    private String modelo;

    @Column(name = "especificacoes_tecnicas", length = 500)
    private String especificacoesTecnicas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusMaterial status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacao;

    @Column(name = "quantidade_disponivel", nullable = false)
    @Builder.Default
    private Integer quantidadeDisponivel = 0;

    @Column(name = "quantidade_minima", nullable = false)
    @Builder.Default
    private Integer quantidadeMinima = 0;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    public void prePersist() {
        if (this.quantidadeDisponivel == null) {
            this.quantidadeDisponivel = 0;
        }
        if (this.quantidadeMinima == null) {
            this.quantidadeMinima = 0;
        }
        if (this.status == null) {
            this.status = StatusMaterial.DISPONIVEL;
        }
        LocalDateTime now = LocalDateTime.now();
        this.criadoEm = now;
        this.atualizadoEm = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getLocalId() {
        return this.localizacao != null ? this.localizacao.getId() : null;
    }

    public boolean isAlertaReposicao() {
        if (this.quantidadeDisponivel == null || this.quantidadeMinima == null) {
            return false;
        }
        return this.quantidadeDisponivel <= this.quantidadeMinima;
    }
}