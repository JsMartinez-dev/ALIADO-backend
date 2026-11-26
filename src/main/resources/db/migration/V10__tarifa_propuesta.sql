-- D-09: tarifa acordada de la propuesta en pesos colombianos.
-- Nullable para conservar propuestas históricas creadas antes de esta decisión.
ALTER TABLE propuestas ADD COLUMN tarifa_cop INTEGER;

ALTER TABLE propuestas ADD CONSTRAINT chk_propuestas_tarifa_positiva
    CHECK (tarifa_cop IS NULL OR tarifa_cop > 0);
