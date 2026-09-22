package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.dto.OrdemProducaoUpdateDTO;
import com.estoque.prensas_api.mapper.OrdemProducaoMapper;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.EstoqueProduzido;
import com.estoque.prensas_api.model.OrdemProducao;
import com.estoque.prensas_api.model.StatusOrdemProducao;
import com.estoque.prensas_api.model.Usuario;
import com.estoque.prensas_api.repository.CaixaChapaRepository;
import com.estoque.prensas_api.repository.EstoqueProduzidoRepository;
import com.estoque.prensas_api.repository.OrdemProducaoRepository;
import com.estoque.prensas_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrdemProducaoService {

    private final OrdemProducaoRepository ordemProducaoRepository;
    private final OrdemProducaoMapper ordemProducaoMapper;
    private final CaixaChapaRepository caixaChapaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueProduzidoRepository estoqueProduzidoRepository;

    public OrdemProducaoService(OrdemProducaoRepository ordemProducaoRepository, OrdemProducaoMapper ordemProducaoMapper,
                                 CaixaChapaRepository caixaChapaRepository, UsuarioRepository usuarioRepository,
                                 EstoqueProduzidoRepository estoqueProduzidoRepository) {
        this.ordemProducaoRepository = ordemProducaoRepository;
        this.ordemProducaoMapper = ordemProducaoMapper;
        this.caixaChapaRepository = caixaChapaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estoqueProduzidoRepository = estoqueProduzidoRepository;
    }

    @Transactional
    public OrdemProducaoResponseDTO create(OrdemProducaoCreateDTO dto) {
        CaixaChapa caixaChapa = caixaChapaRepository.findById(dto.caixaChapaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caixa de chapa não encontrada: " + dto.caixaChapaId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + dto.usuarioId()));

        OrdemProducao ordemProducao = ordemProducaoMapper.toEntity(dto, caixaChapa, usuario);
        ordemProducao.setStatus(StatusOrdemProducao.PLANEJADA);
        return ordemProducaoMapper.toResponseDTO(ordemProducaoRepository.save(ordemProducao));
    }

    @Transactional(readOnly = true)
    public OrdemProducaoResponseDTO findById(Long id) {
        return ordemProducaoMapper.toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrdemProducaoResponseDTO> findAll() {
        return ordemProducaoRepository.findAll().stream()
                .map(ordemProducaoMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public OrdemProducaoResponseDTO update(Long id, OrdemProducaoUpdateDTO dto) {
        OrdemProducao ordemProducao = getOrThrow(id);
        ordemProducao.setIdPrensa(dto.idPrensa());
        ordemProducao.setQuantidadeAProcessar(dto.quantidadeAProcessar());
        ordemProducao.setStatus(dto.status());

        if (dto.estoqueProduzidoId() != null) {
            EstoqueProduzido estoqueProduzido = estoqueProduzidoRepository.findById(dto.estoqueProduzidoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque produzido não encontrado: " + dto.estoqueProduzidoId()));
            ordemProducao.setEstoqueProduzido(estoqueProduzido);
        } else {
            ordemProducao.setEstoqueProduzido(null);
        }

        return ordemProducaoMapper.toResponseDTO(ordemProducaoRepository.save(ordemProducao));
    }

    @Transactional
    public void delete(Long id) {
        if (!ordemProducaoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de Produção " + id + " não encontrada");
        }
        ordemProducaoRepository.deleteById(id);
    }

    private OrdemProducao getOrThrow(Long id) {
        return ordemProducaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de Produção " + id + " não encontrada"));
    }
}