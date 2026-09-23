package com.estoque.prensas_api.controller;

import com.estoque.prensas_api.dto.EstoqueProduzidoCreateDTO;
import com.estoque.prensas_api.dto.EstoqueProduzidoResponseDTO;
import com.estoque.prensas_api.dto.EstoqueProduzidoUpdateDTO;
import com.estoque.prensas_api.service.EstoqueProduzidoService;
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
@RequestMapping("/api/estoques-produzidos")
public class EstoqueProduzidoController {

    private final EstoqueProduzidoService estoqueProduzidoService;

    public EstoqueProduzidoController(EstoqueProduzidoService estoqueProduzidoService) {
        this.estoqueProduzidoService = estoqueProduzidoService;
    }

    @PostMapping
    public ResponseEntity<EstoqueProduzidoResponseDTO> create(@Valid @RequestBody EstoqueProduzidoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueProduzidoService.create(dto));
    }

    @GetMapping("/{id}")
    public EstoqueProduzidoResponseDTO findById(@PathVariable Long id) {
        return estoqueProduzidoService.findById(id);
    }

    @GetMapping
    public List<EstoqueProduzidoResponseDTO> findAll() {
        return estoqueProduzidoService.findAll();
    }

    @PutMapping("/{id}")
    public EstoqueProduzidoResponseDTO update(@PathVariable Long id, @Valid @RequestBody EstoqueProduzidoUpdateDTO dto) {
        return estoqueProduzidoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        estoqueProduzidoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
