ALTER TABLE material ADD COLUMN modelo VARCHAR(100);
ALTER TABLE material ADD COLUMN especificacoes_tecnicas VARCHAR(500);
CREATE INDEX idx_material_nome ON material(nome);

ALTER TABLE material ADD CONSTRAINT chk_material_categoria
    CHECK (categoria IN (
        'PERIFERICO', 'HARDWARE', 'CABO', 'COMPONENTE_ELETRONICO',
        'EQUIPAMENTO', 'FERRAMENTA', 'CONSUMIVEL'
    ));

ALTER TABLE material ADD CONSTRAINT chk_material_status
    CHECK (status IN ('DISPONIVEL', 'EM_MANUTENCAO', 'OBSOLETO', 'INDISPONIVEL'));
