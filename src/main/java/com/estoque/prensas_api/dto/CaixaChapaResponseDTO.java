package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.StatusCaixaChapa;

import java.time.LocalDateTime;

public record CaixaChapaResponseDTO(
        Long id,
        String nome,
        String tamanho,
        Double espessura,
        Integer quantidade,
        LocalDateTime dataEntrada,
        StatusCaixaChapa status
) {
}
