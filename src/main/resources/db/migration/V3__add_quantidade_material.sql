ALTER TABLE material ADD COLUMN quantidade_disponivel INT NOT NULL DEFAULT 0;
ALTER TABLE material ADD COLUMN quantidade_minima INT NOT NULL DEFAULT 0;
ALTER TABLE material ADD CONSTRAINT chk_material_quantidade_disponivel CHECK (quantidade_disponivel >= 0);
ALTER TABLE material ADD CONSTRAINT chk_material_quantidade_minima CHECK (quantidade_minima >= 0);
