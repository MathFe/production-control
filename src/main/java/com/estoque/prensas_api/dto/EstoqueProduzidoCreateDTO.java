package com.estoque.prensas_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EstoqueProduzidoCreateDTO(
        @NotBlank
        @Size(max = 150)
        String nomePeca,

        @NotNull
        @PositiveOrZero
        Integer quantidade,

        @NotNull
        @PositiveOrZero
        Integer quantidadeMinima

) {
}
