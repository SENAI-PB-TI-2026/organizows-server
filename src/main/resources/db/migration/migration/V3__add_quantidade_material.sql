ALTER TABLE material
    ADD COLUMN quantidade_disponivel INT NOT NULL DEFAULT 0,
ADD COLUMN quantidade_minima INT NOT NULL DEFAULT 0;