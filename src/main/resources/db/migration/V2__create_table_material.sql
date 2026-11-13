CREATE TABLE material (
                          id BIGSERIAL PRIMARY KEY,
                          descricao VARCHAR(150) NOT NULL,
                          categoria VARCHAR(50) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          local_id BIGINT,
                          criado_em TIMESTAMP NOT NULL,
                          atualizado_em TIMESTAMP NOT NULL,
                          CONSTRAINT fk_material_local FOREIGN KEY (local_id) REFERENCES local(id)
);