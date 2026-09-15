package com.estoque.prensas_api.repository;

import com.estoque.prensas_api.model.OrdemProducao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemProducaoRepository extends JpaRepository<OrdemProducao, Long> {
}
