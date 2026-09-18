package com.estoque.prensas_api.controller;

import com.estoque.prensas_api.dto.CaixaChapaCreateDTO;
import com.estoque.prensas_api.dto.CaixaChapaResponseDTO;
import com.estoque.prensas_api.dto.CaixaChapaUpdateDTO;
import com.estoque.prensas_api.service.CaixaChapaService;
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
@RequestMapping("/api/caixas-chapa")
public class CaixaChapaController {

    private final CaixaChapaService caixaChapaService;

    public CaixaChapaController(CaixaChapaService caixaChapaService) {
        this.caixaChapaService = caixaChapaService;
    }

    @PostMapping
    public ResponseEntity<CaixaChapaResponseDTO> create(@Valid @RequestBody CaixaChapaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caixaChapaService.create(dto));
    }

    @GetMapping("/{id}")
    public CaixaChapaResponseDTO findById(@PathVariable Long id) {
        return caixaChapaService.findById(id);
    }

    @GetMapping
    public List<CaixaChapaResponseDTO> findAll() {
        return caixaChapaService.findAll();
    }

    @PutMapping("/{id}")
    public CaixaChapaResponseDTO update(@PathVariable Long id, @Valid @RequestBody CaixaChapaUpdateDTO dto) {
        return caixaChapaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        caixaChapaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
