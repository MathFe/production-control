package com.estoque.prensas_api.mapper;

import com.estoque.prensas_api.dto.UsuarioRequestDTO;
import com.estoque.prensas_api.dto.UsuarioResponseDTO;
import com.estoque.prensas_api.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());
        usuario.setPassword(dto.password());
        return usuario;
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getDataRegistro()
        );
    }
}
