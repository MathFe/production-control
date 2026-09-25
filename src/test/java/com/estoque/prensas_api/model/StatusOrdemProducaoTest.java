package com.estoque.prensas_api.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StatusOrdemProducaoTest {

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "PLANEJADA,        EM_PROCESSAMENTO, true",
            "EM_PROCESSAMENTO, CONCLUIDA,        true",
            "PLANEJADA,        PLANEJADA,        false",
            "PLANEJADA,        CONCLUIDA,        false",
            "EM_PROCESSAMENTO, PLANEJADA,        false",
            "EM_PROCESSAMENTO, EM_PROCESSAMENTO, false",
            "CONCLUIDA,        PLANEJADA,        false",
            "CONCLUIDA,        EM_PROCESSAMENTO, false",
            "CONCLUIDA,        CONCLUIDA,        false"
    })
    void podeIrPara(StatusOrdemProducao atual, StatusOrdemProducao novo, boolean esperado) {
        assertThat(atual.podeIrPara(novo)).isEqualTo(esperado);
    }
}
