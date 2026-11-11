package br.senai.organizows.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_pecas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 80)
    private String codigo; // Part Number / Código de identificação do componente

    @Column(length = 80)
    private String categoria; // Ex: Resistores, Capacitores, CI, Módulos, Sensores

    @Column(columnDefinition = "TEXT")
    private String descricao; // Especificações técnicas, encapsulamento (ex: SMD, DIP, etc.)

    @Column(name = "quantidade_estoque", nullable = false)
    @Builder.Default
    private Integer quantidadeEstoque = 0;

    @Column(name = "quantidade_minima")
    @Builder.Default
    private Integer quantidadeMinima = 5;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(length = 100)
    private String localizacao; // Ex: Gaveteiro 1 - Gaveta C4, Bancada 2
}
