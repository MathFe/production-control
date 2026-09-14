CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    data_registro TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE caixa_chapa (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    tamanho      VARCHAR(50) NOT NULL,
    espessura    DOUBLE PRECISION NOT NULL,
    quantidade   INTEGER NOT NULL,
    data_entrada TIMESTAMP NOT NULL DEFAULT now(),
    status       VARCHAR(20) NOT NULL CHECK (status IN ('DISPONIVEL', 'EM_PRODUCAO', 'FINALIZADA'))
);

CREATE TABLE estoque_produzido (
    id                BIGSERIAL PRIMARY KEY,
    nome_peca         VARCHAR(150) NOT NULL,
    quantidade        INTEGER NOT NULL DEFAULT 0,
    quantidade_minima INTEGER NOT NULL DEFAULT 0,
    nivel_estoque     VARCHAR(20) NOT NULL CHECK (nivel_estoque IN ('ALTA', 'MEDIA', 'CRITICA')),
    data_chegada      TIMESTAMP NOT NULL DEFAULT now()
);

-- id_prensa fica sem FK por enquanto: Prensa e opcional no MVP e ainda nao foi modelada.
CREATE TABLE ordem_producao (
    id                      BIGSERIAL PRIMARY KEY,
    caixa_chapa_id          BIGINT NOT NULL UNIQUE REFERENCES caixa_chapa (id),
    usuario_id              BIGINT NOT NULL REFERENCES usuario (id),
    estoque_produzido_id    BIGINT REFERENCES estoque_produzido (id),
    id_prensa               BIGINT,
    quantidade_a_processar  INTEGER NOT NULL,
    data_ordem              TIMESTAMP NOT NULL DEFAULT now(),
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('PLANEJADA', 'EM_PROCESSAMENTO', 'CONCLUIDA'))
);

CREATE INDEX idx_ordem_producao_usuario ON ordem_producao (usuario_id);
CREATE INDEX idx_ordem_producao_estoque_produzido ON ordem_producao (estoque_produzido_id);
