package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.CaixaChapaCreateDTO;
import com.estoque.prensas_api.dto.CaixaChapaResponseDTO;
import com.estoque.prensas_api.dto.CaixaChapaUpdateDTO;
import com.estoque.prensas_api.mapper.CaixaChapaMapper;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.StatusCaixaChapa;
import com.estoque.prensas_api.repository.CaixaChapaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CaixaChapaService {

    private final CaixaChapaRepository caixaChapaRepository;
    private final CaixaChapaMapper caixaChapaMapper;

    public CaixaChapaService(CaixaChapaRepository caixaChapaRepository, CaixaChapaMapper caixaChapaMapper) {
        this.caixaChapaRepository = caixaChapaRepository;
        this.caixaChapaMapper = caixaChapaMapper;
    }

    @Transactional
    public CaixaChapaResponseDTO create (CaixaChapaCreateDTO dto) {
        CaixaChapa caixaChapa = caixaChapaMapper.toEntity(dto);
        caixaChapa.setStatus(StatusCaixaChapa.DISPONIVEL);
        return caixaChapaMapper.toResponseDTO(caixaChapaRepository.save(caixaChapa));
    }

    @Transactional(readOnly = true)
    public CaixaChapaResponseDTO findById(Long id) {
        return caixaChapaMapper.toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CaixaChapaResponseDTO> findAll() {
        return caixaChapaRepository.findAll().stream()
                .map(caixaChapaMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public CaixaChapaResponseDTO update(Long id, CaixaChapaUpdateDTO dto) {
        CaixaChapa caixaChapa = getOrThrow(id);
        caixaChapa.setNome(dto.nome());
        caixaChapa.setQuantidade(dto.quantidade());
        caixaChapa.setEspessura(dto.espessura());
        caixaChapa.setTamanho(dto.tamanho());
        caixaChapa.setStatus(dto.status());
        return caixaChapaMapper.toResponseDTO(caixaChapaRepository.save(caixaChapa));
    }

    @Transactional
    public void delete(Long id) {
        if (!caixaChapaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Caixa de chapa não encontrada: " + id);
        }
        caixaChapaRepository.deleteById(id);
    }

    private CaixaChapa getOrThrow(Long id) {
        return caixaChapaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caixa de chapa não encontrada: " + id));
    }
}
