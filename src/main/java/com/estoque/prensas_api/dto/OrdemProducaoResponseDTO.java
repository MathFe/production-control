package com.estoque.prensas_api.dto;

import com.estoque.prensas_api.model.StatusOrdemProducao;

import java.time.LocalDateTime;

public record OrdemProducaoResponseDTO(

        Long id,
        Long caixaChapaId,
        Long usuarioId,
        Long estoqueProduzidoId,
        Long idPrensa,
        Integer quantidadeAProcessar,
        LocalDateTime dataOrdem,
        StatusOrdemProducao status

) {
}
