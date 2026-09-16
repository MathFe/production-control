package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.UsuarioRequestDTO;
import com.estoque.prensas_api.dto.UsuarioResponseDTO;
import com.estoque.prensas_api.mapper.UsuarioMapper;
import com.estoque.prensas_api.model.Usuario;
import com.estoque.prensas_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO findById(Long id)   {
        return usuarioMapper.toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = getOrThrow(id);
        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario getOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + id));
    }
}
