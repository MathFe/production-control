package com.estoque.prensas_api.mapper;

import com.estoque.prensas_api.dto.CaixaChapaCreateDTO;
import com.estoque.prensas_api.dto.CaixaChapaResponseDTO;
import com.estoque.prensas_api.model.CaixaChapa;
import org.springframework.stereotype.Component;

@Component
public class CaixaChapaMapper {

    public CaixaChapa toEntity(CaixaChapaCreateDTO dto) {
        CaixaChapa caixaChapa = new CaixaChapa();
        caixaChapa.setNome(dto.nome());
        caixaChapa.setTamanho(dto.tamanho());
        caixaChapa.setEspessura(dto.espessura());
        caixaChapa.setQuantidade(dto.quantidade());

        return caixaChapa;
    }

    public CaixaChapaResponseDTO toResponseDTO(CaixaChapa caixaChapa) {
        return new CaixaChapaResponseDTO(
                caixaChapa.getId(),
                caixaChapa.getNome(),
                caixaChapa.getTamanho(),
                caixaChapa.getEspessura(),
                caixaChapa.getQuantidade(),
                caixaChapa.getDataEntrada(),
                caixaChapa.getStatus()
        );
    }


}
