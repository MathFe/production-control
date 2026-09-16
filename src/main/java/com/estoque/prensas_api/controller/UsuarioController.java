package com.estoque.prensas_api.controller;

import com.estoque.prensas_api.dto.UsuarioRequestDTO;
import com.estoque.prensas_api.dto.UsuarioResponseDTO;
import com.estoque.prensas_api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(dto));
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO findById(@PathVariable Long id) {
        return usuarioService.findById(id);
    }

    @GetMapping
    public List<UsuarioResponseDTO> findAll() {
        return usuarioService.findAll();
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO update(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
