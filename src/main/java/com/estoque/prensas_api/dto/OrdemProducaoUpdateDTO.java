package com.estoque.prensas_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdemProducaoUpdateDTO(

        Long idPrensa,

        @NotNull
        @Positive
        Integer quantidadeAProcessar,

        Long estoqueProduzidoId

) {
}
