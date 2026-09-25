package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.EstoqueProduzidoCreateDTO;
import com.estoque.prensas_api.dto.EstoqueProduzidoResponseDTO;
import com.estoque.prensas_api.dto.EstoqueProduzidoUpdateDTO;
import com.estoque.prensas_api.mapper.EstoqueProduzidoMapper;
import com.estoque.prensas_api.model.EstoqueProduzido;
import com.estoque.prensas_api.repository.EstoqueProduzidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EstoqueProduzidoService {

    private final EstoqueProduzidoRepository estoqueProduzidoRepository;
    private final EstoqueProduzidoMapper estoqueProduzidoMapper;

    public EstoqueProduzidoService(EstoqueProduzidoRepository estoqueProduzidoRepository, EstoqueProduzidoMapper estoqueProduzidoMapper) {
        this.estoqueProduzidoRepository = estoqueProduzidoRepository;
        this.estoqueProduzidoMapper = estoqueProduzidoMapper;
    }

    @Transactional
    public EstoqueProduzidoResponseDTO create(EstoqueProduzidoCreateDTO dto) {
        EstoqueProduzido estoqueProduzido = estoqueProduzidoMapper.toEntity(dto);
        estoqueProduzido.recalcularNivel();
        return estoqueProduzidoMapper.toResponseDTO(estoqueProduzidoRepository.save(estoqueProduzido));
    }

    @Transactional(readOnly = true)
    public EstoqueProduzidoResponseDTO findById(Long id) {
        return estoqueProduzidoMapper.toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EstoqueProduzidoResponseDTO> findAll() {
        return estoqueProduzidoRepository.findAll().stream()
                .map(estoqueProduzidoMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public EstoqueProduzidoResponseDTO update(Long id, EstoqueProduzidoUpdateDTO dto) {
        EstoqueProduzido estoqueProduzido = getOrThrow(id);
        estoqueProduzido.setNomePeca(dto.nomePeca());
        estoqueProduzido.setQuantidade(dto.quantidade());
        estoqueProduzido.setQuantidadeMinima(dto.quantidadeMinima());
        estoqueProduzido.recalcularNivel();
        return estoqueProduzidoMapper.toResponseDTO(estoqueProduzidoRepository.save(estoqueProduzido));
    }

    @Transactional
    public void delete(Long id) {
        if (!estoqueProduzidoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque produzido não encontrado: " + id);
        }
        estoqueProduzidoRepository.deleteById(id);
    }

    private EstoqueProduzido getOrThrow(Long id) {
        return estoqueProduzidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque produzido não encontrado: " + id));
    }
}
