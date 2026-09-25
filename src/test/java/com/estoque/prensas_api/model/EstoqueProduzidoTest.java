package com.estoque.prensas_api.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EstoqueProduzidoTest {

    @ParameterizedTest(name = "quantidade {0}, mínima {1} -> {2}")
    @CsvSource({
            "0,  20, CRITICA",
            "20, 20, CRITICA",
            "21, 20, MEDIA",
            "40, 20, MEDIA",
            "41, 20, ALTA"
    })
    void recalcularNivel(int quantidade, int quantidadeMinima, NivelEstoque esperado) {
        EstoqueProduzido estoque = estoque(quantidade, quantidadeMinima);

        estoque.recalcularNivel();

        assertThat(estoque.getNivelEstoque()).isEqualTo(esperado);
    }

    @Test
    void adicionarQuantidadeSomaERecalculaNivel() {
        EstoqueProduzido estoque = estoque(10, 20);
        estoque.recalcularNivel();

        estoque.adicionarQuantidade(50);

        assertThat(estoque.getQuantidade()).isEqualTo(60);
        assertThat(estoque.getNivelEstoque()).isEqualTo(NivelEstoque.ALTA);
    }

    private EstoqueProduzido estoque(int quantidade, int quantidadeMinima) {
        EstoqueProduzido estoque = new EstoqueProduzido();
        estoque.setQuantidade(quantidade);
        estoque.setQuantidadeMinima(quantidadeMinima);
        return estoque;
    }
}
