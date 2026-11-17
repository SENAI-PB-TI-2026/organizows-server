package br.senai.organizows.model;

import br.senai.organizows.exception.HierarquiaInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "localizacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Localizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nome;

    @Size(max = 100)
    @Column(length = 100)
    private String tipo;

    @Size(max = 255)
    @Column(length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_pai_id")
    @ToString.Exclude
    private Localizacao localPai;

    public void setLocalPai(Localizacao localPai) {
        Localizacao anterior = this.localPai;
        this.localPai = localPai;
        if (localPai != null) {
            try {
                validarHierarquia();
            } catch (RuntimeException e) {
                this.localPai = anterior;
                throw e;
            }
        }
    }

    public Localizacao getLocalizacaoPai() {
        return this.localPai;
    }

    public void setLocalizacaoPai(Localizacao localizacaoPai) {
        setLocalPai(localizacaoPai);
    }

    @PrePersist
    @PreUpdate
    public void validarHierarquia() {
        if (this.localPai == null) {
            return;
        }

        if (this == this.localPai || (this.id != null && this.id.equals(this.localPai.getId()))) {
            throw new HierarquiaInvalidaException("Uma localização não pode ter a si mesma como pai.");
        }

        Localizacao atual = this.localPai;
        Set<Long> idsVisitados = new HashSet<>();
        Set<Integer> instanciasVisitadas = new HashSet<>();

        if (this.id != null) {
            idsVisitados.add(this.id);
        }
        instanciasVisitadas.add(System.identityHashCode(this));

        while (atual != null) {
            if (atual == this || !instanciasVisitadas.add(System.identityHashCode(atual))) {
                throw new HierarquiaInvalidaException(
                        "Ciclo detectado na hierarquia de localizações: uma localização não pode ser descendente de si mesma."
                );
            }

            if (atual.getId() != null) {
                if (atual.getId().equals(this.id) || !idsVisitados.add(atual.getId())) {
                    throw new HierarquiaInvalidaException(
                            "Ciclo detectado na hierarquia de localizações: uma localização não pode ser descendente de si mesma."
                    );
                }
            }
            atual = atual.getLocalPai();
        }
    }

    public List<Localizacao> getCaminhoHierarquia() {
        List<Localizacao> caminho = new ArrayList<>();
        Localizacao atual = this;
        Set<Object> visitados = new HashSet<>();
        while (atual != null) {
            Object key = atual.getId() != null ? atual.getId() : atual;
            if (!visitados.add(key)) {
                throw new HierarquiaInvalidaException("Ciclo detectado na hierarquia ao montar caminho.");
            }
            caminho.add(0, atual);
            atual = atual.getLocalPai();
        }
        return Collections.unmodifiableList(caminho);
    }

    public String getEnderecoCompleto() {
        return getEnderecoCompleto(" > ");
    }

    public String getEnderecoCompleto(String separador) {
        String sep = separador != null ? separador : " > ";
        return getCaminhoHierarquia().stream()
                .map(Localizacao::getNome)
                .collect(Collectors.joining(sep));
    }

    public String getEnderecoCodigoCompleto() {
        return getEnderecoCodigoCompleto(" > ");
    }

    public String getEnderecoCodigoCompleto(String separador) {
        String sep = separador != null ? separador : " > ";
        return getCaminhoHierarquia().stream()
                .map(Localizacao::getCodigo)
                .collect(Collectors.joining(sep));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localizacao that = (Localizacao) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
