package com.estoque.prensas_api.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String username,
        String email,
        LocalDateTime dataRegistro
) {
}
