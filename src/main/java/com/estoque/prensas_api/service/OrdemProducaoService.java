package com.estoque.prensas_api.service;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.dto.OrdemProducaoUpdateDTO;
import com.estoque.prensas_api.mapper.OrdemProducaoMapper;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.EstoqueProduzido;
import com.estoque.prensas_api.model.OrdemProducao;
import com.estoque.prensas_api.model.StatusCaixaChapa;
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

        if (caixaChapa.getStatus() != StatusCaixaChapa.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Caixa de chapa " + caixaChapa.getId() + " não está disponível (status: " + caixaChapa.getStatus() + ")");
        }
        validarQuantidade(dto.quantidadeAProcessar(), caixaChapa);

        OrdemProducao ordemProducao = ordemProducaoMapper.toEntity(dto, caixaChapa, usuario);
        ordemProducao.setStatus(StatusOrdemProducao.PLANEJADA);
        caixaChapa.setStatus(StatusCaixaChapa.EM_PRODUCAO);
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
        exigirPlanejada(ordemProducao, "editada");
        validarQuantidade(dto.quantidadeAProcessar(), ordemProducao.getCaixaChapa());

        ordemProducao.setIdPrensa(dto.idPrensa());
        ordemProducao.setQuantidadeAProcessar(dto.quantidadeAProcessar());

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
    public OrdemProducaoResponseDTO alterarStatus(Long id, StatusOrdemProducao novoStatus) {
        OrdemProducao ordemProducao = getOrThrow(id);

        if (!ordemProducao.getStatus().podeIrPara(novoStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transição inválida: " + ordemProducao.getStatus() + " -> " + novoStatus);
        }

        if (novoStatus == StatusOrdemProducao.EM_PROCESSAMENTO) {
            // Depois de PLANEJADA a ordem não pode mais ser editada, então o destino precisa estar definido aqui
            exigirEstoqueVinculado(ordemProducao, "iniciada");
        }
        if (novoStatus == StatusOrdemProducao.CONCLUIDA) {
            concluir(ordemProducao);
        }

        ordemProducao.setStatus(novoStatus);
        return ordemProducaoMapper.toResponseDTO(ordemProducao);
    }

    @Transactional
    public void delete(Long id) {
        OrdemProducao ordemProducao = getOrThrow(id);
        exigirPlanejada(ordemProducao, "excluída");

        ordemProducao.getCaixaChapa().setStatus(StatusCaixaChapa.DISPONIVEL);
        ordemProducaoRepository.delete(ordemProducao);
    }

    private void concluir(OrdemProducao ordemProducao) {
        exigirEstoqueVinculado(ordemProducao, "concluída");

        ordemProducao.getEstoqueProduzido().adicionarQuantidade(ordemProducao.getQuantidadeAProcessar());
        ordemProducao.getCaixaChapa().setStatus(StatusCaixaChapa.FINALIZADA);
    }

    private void exigirEstoqueVinculado(OrdemProducao ordemProducao, String acao) {
        if (ordemProducao.getEstoqueProduzido() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ordem de Produção " + ordemProducao.getId() + " não pode ser " + acao + " sem estoque produzido vinculado");
        }
    }

    private void exigirPlanejada(OrdemProducao ordemProducao, String acao) {
        if (ordemProducao.getStatus() != StatusOrdemProducao.PLANEJADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ordem de Produção " + ordemProducao.getId() + " não pode ser " + acao + " (status: " + ordemProducao.getStatus() + ")");
        }
    }

    private void validarQuantidade(Integer quantidadeAProcessar, CaixaChapa caixaChapa) {
        if (quantidadeAProcessar > caixaChapa.getQuantidade()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Quantidade a processar (" + quantidadeAProcessar + ") maior que a quantidade da caixa (" + caixaChapa.getQuantidade() + ")");
        }
    }

    private OrdemProducao getOrThrow(Long id) {
        return ordemProducaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de Produção " + id + " não encontrada"));
    }
}
