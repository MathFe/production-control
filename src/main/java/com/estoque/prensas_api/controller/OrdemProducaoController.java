package com.estoque.prensas_api.controller;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.dto.OrdemProducaoStatusDTO;
import com.estoque.prensas_api.dto.OrdemProducaoUpdateDTO;
import com.estoque.prensas_api.service.OrdemProducaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-producao")
public class OrdemProducaoController {

    private final OrdemProducaoService ordemProducaoService;

    public OrdemProducaoController(OrdemProducaoService ordemProducaoService) {
        this.ordemProducaoService = ordemProducaoService;
    }

    @PostMapping
    public ResponseEntity<OrdemProducaoResponseDTO> create(@Valid @RequestBody OrdemProducaoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemProducaoService.create(dto));
    }

    @GetMapping("/{id}")
    public OrdemProducaoResponseDTO findById(@PathVariable Long id) {
        return ordemProducaoService.findById(id);
    }

    @GetMapping
    public List<OrdemProducaoResponseDTO> findAll() {
        return ordemProducaoService.findAll();
    }

    @PutMapping("/{id}")
    public OrdemProducaoResponseDTO update(@PathVariable Long id, @Valid @RequestBody OrdemProducaoUpdateDTO dto) {
        return ordemProducaoService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public OrdemProducaoResponseDTO alterarStatus(@PathVariable Long id, @Valid @RequestBody OrdemProducaoStatusDTO dto) {
        return ordemProducaoService.alterarStatus(id, dto.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordemProducaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
