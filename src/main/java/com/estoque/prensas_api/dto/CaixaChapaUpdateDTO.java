package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.StatusCaixaChapa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CaixaChapaUpdateDTO(
        @NotBlank
        @Size(max = 100)
        String nome,

        @NotBlank
        @Size(max = 30)
        String tamanho,

        @NotNull
        @Positive
        Double espessura,

        @Positive
        @NotNull
        Integer quantidade,

        @NotNull
        StatusCaixaChapa status

) {
}
