package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.mapper.OrdemProducaoMapper;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.OrdemProducao;
import com.estoque.prensas_api.model.StatusOrdemProducao;
import com.estoque.prensas_api.model.Usuario;
import com.estoque.prensas_api.repository.CaixaChapaRepository;
import com.estoque.prensas_api.repository.OrdemProducaoRepository;
import com.estoque.prensas_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrdemProducaoService {

    private final OrdemProducaoRepository ordemProducaoRepository;
    private final OrdemProducaoMapper ordemProducaoMapper;
    private final CaixaChapaRepository caixaChapaRepository;
    private final UsuarioRepository usuarioRepository;

    public OrdemProducaoService(OrdemProducaoRepository ordemProducaoRepository, OrdemProducaoMapper ordemProducaoMapper,
                                 CaixaChapaRepository caixaChapaRepository, UsuarioRepository usuarioRepository) {
        this.ordemProducaoRepository = ordemProducaoRepository;
        this.ordemProducaoMapper = ordemProducaoMapper;
        this.caixaChapaRepository = caixaChapaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    @Transactional
    public OrdemProducaoResponseDTO create (OrdemProducaoCreateDTO dto) {
        CaixaChapa caixaChapa = caixaChapaRepository.findById(dto.caixaChapaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caixa de chapa não encontrada: " + dto.caixaChapaId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + dto.usuarioId()));

        OrdemProducao ordemProducao = ordemProducaoMapper.toEntity(dto, caixaChapa, usuario);
        ordemProducao.setStatus(StatusOrdemProducao.PLANEJADA);
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
