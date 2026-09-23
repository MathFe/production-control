package com.estoque.prensas_api.mapper;

import com.estoque.prensas_api.dto.EstoqueProduzidoCreateDTO;
import com.estoque.prensas_api.dto.EstoqueProduzidoResponseDTO;
import com.estoque.prensas_api.model.EstoqueProduzido;
import org.springframework.stereotype.Component;

@Component
public class EstoqueProduzidoMapper {

    public EstoqueProduzido toEntity(EstoqueProduzidoCreateDTO dto) {
        EstoqueProduzido estoqueProduzido = new EstoqueProduzido();
        estoqueProduzido.setNomePeca(dto.nomePeca());
        estoqueProduzido.setQuantidade(dto.quantidade());
        estoqueProduzido.setQuantidadeMinima(dto.quantidadeMinima());

        return estoqueProduzido;
    }

    public EstoqueProduzidoResponseDTO toResponseDTO(EstoqueProduzido estoqueProduzido) {
        return new EstoqueProduzidoResponseDTO(
                estoqueProduzido.getId(),
                estoqueProduzido.getNomePeca(),
                estoqueProduzido.getQuantidade(),
                estoqueProduzido.getQuantidadeMinima(),
                estoqueProduzido.getNivelEstoque(),
                estoqueProduzido.getDataChegada()
        );
    }
}
