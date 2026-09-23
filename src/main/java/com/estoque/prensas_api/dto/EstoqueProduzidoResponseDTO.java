package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.NivelEstoque;

import java.time.LocalDateTime;

public record EstoqueProduzidoResponseDTO(
        Long id,
        String nomePeca,
        Integer quantidade,
        Integer quantidadeMinima,
        NivelEstoque nivelEstoque,
        LocalDateTime dataChegada
) {
}
