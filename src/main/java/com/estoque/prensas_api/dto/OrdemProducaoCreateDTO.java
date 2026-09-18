package com.estoque.prensas_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdemProducaoCreateDTO(

        @NotNull
        Long caixaChapaId,

        @NotNull
        Long usuarioId,

        Long idPrensa,

        @NotNull
        @Positive
        Integer quantidadeAProcessar

) {
}
