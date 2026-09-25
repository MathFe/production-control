package com.estoque.prensas_api.model;

public enum StatusOrdemProducao {
    PLANEJADA,
    EM_PROCESSAMENTO,
    CONCLUIDA;

    public boolean podeIrPara(StatusOrdemProducao novo) {
        return switch (this) {
            case PLANEJADA -> novo == EM_PROCESSAMENTO;
            case EM_PROCESSAMENTO -> novo == CONCLUIDA;
            case CONCLUIDA -> false;
        };
    }
}
