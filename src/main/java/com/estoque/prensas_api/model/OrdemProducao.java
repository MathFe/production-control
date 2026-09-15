package com.estoque.prensas_api.model;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_producao")
@Getter
@Setter
@NoArgsConstructor
public class OrdemProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_chapa_id", nullable = false, unique = true)
    private CaixaChapa caixaChapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estoque_produzido_id")
    private EstoqueProduzido estoqueProduzido;

    @Column(name = "id_prensa")
    private Long idPrensa;

    @Column(name = "quantidade_a_processar", nullable = false)
    private Integer quantidadeAProcessar;

    @CreationTimestamp
    @Column(name = "data_ordem", nullable = false, updatable = false)
    private LocalDateTime dataOrdem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrdemProducao status;
}
