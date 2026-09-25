package com.estoque.prensas_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "estoque_produzido")
@Getter
@Setter
@NoArgsConstructor
public class EstoqueProduzido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_peca", nullable = false, length = 150)
    private String nomePeca;

    @Column(nullable = false)
    private Integer quantidade = 0;

    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_estoque", nullable = false, length = 20)
    private NivelEstoque nivelEstoque;

    @CreationTimestamp
    @Column(name = "data_chegada", nullable = false, updatable = false)
    private LocalDateTime dataChegada;

    public void adicionarQuantidade(int quantidadeAdicional) {
        this.quantidade += quantidadeAdicional;
        recalcularNivel();
    }

    public void recalcularNivel() {
        if (quantidade <= quantidadeMinima) {
            this.nivelEstoque = NivelEstoque.CRITICA;
        } else if (quantidade <= quantidadeMinima * 2) {
            this.nivelEstoque = NivelEstoque.MEDIA;
        } else {
            this.nivelEstoque = NivelEstoque.ALTA;
        }
    }
}
