package com.estoque.prensas_api.repository;

import com.estoque.prensas_api.model.EstoqueProduzido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueProduzidoRepository extends JpaRepository<EstoqueProduzido, Long> {
}
