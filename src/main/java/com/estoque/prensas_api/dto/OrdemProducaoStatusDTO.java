package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.StatusOrdemProducao;
import jakarta.validation.constraints.NotNull;

public record OrdemProducaoStatusDTO(

        @NotNull
        StatusOrdemProducao status

) {
}
