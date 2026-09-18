package com.estoque.prensas_api.mapper;

import com.estoque.prensas_api.dto.OrdemProducaoCreateDTO;
import com.estoque.prensas_api.dto.OrdemProducaoResponseDTO;
import com.estoque.prensas_api.model.CaixaChapa;
import com.estoque.prensas_api.model.OrdemProducao;
import com.estoque.prensas_api.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class OrdemProducaoMapper {

    public OrdemProducao toEntity(OrdemProducaoCreateDTO dto, CaixaChapa caixaChapa, Usuario usuario) {
        OrdemProducao ordemProducao = new OrdemProducao();
        ordemProducao.setCaixaChapa(caixaChapa);
        ordemProducao.setUsuario(usuario);
        ordemProducao.setIdPrensa(dto.idPrensa());
        ordemProducao.setQuantidadeAProcessar(dto.quantidadeAProcessar());

        return ordemProducao;
    }

    public OrdemProducaoResponseDTO toResponseDTO(OrdemProducao ordemProducao) {
        Long estoqueProduzidoId = ordemProducao.getEstoqueProduzido() != null
                ? ordemProducao.getEstoqueProduzido().getId()
                : null;

        return new OrdemProducaoResponseDTO(
                ordemProducao.getId(),
                ordemProducao.getCaixaChapa().getId(),
                ordemProducao.getUsuario().getId(),
                estoqueProduzidoId,
                ordemProducao.getIdPrensa(),
                ordemProducao.getQuantidadeAProcessar(),
                ordemProducao.getDataOrdem(),
                ordemProducao.getStatus()
        );
    }
}
