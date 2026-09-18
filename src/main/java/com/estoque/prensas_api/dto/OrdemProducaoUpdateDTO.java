package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.StatusOrdemProducao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdemProducaoUpdateDTO(

        Long idPrensa,

        @NotNull
        @Positive
        Integer quantidadeAProcessar,

        @NotNull
        StatusOrdemProducao status,

        Long estoqueProduzidoId

) {
}
