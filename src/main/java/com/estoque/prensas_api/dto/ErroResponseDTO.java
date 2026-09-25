package com.estoque.prensas_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponseDTO(

        LocalDateTime timestamp,

        int status,

        String erro,

        String mensagem,

        String path,

        Map<String, String> campos

) {
}
