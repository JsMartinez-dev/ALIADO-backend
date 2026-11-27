-- =============================================================
-- V10: fecha de primera lectura de los avisos de la bandeja
-- Specs: SPEC-09 (notificaciones). F09-BANDEJA-01.
-- Aditiva sobre V4/V9: no modifica columnas, restricciones ni índices existentes.
-- Las filas previas quedan pendientes (leida_en NULL) porque el backend no
-- conservaba historial de lectura; sus datos de entrega no cambian.
-- =============================================================

ALTER TABLE notificaciones ADD COLUMN leida_en TIMESTAMPTZ;

-- Bandeja: avisos de un destinatario ordenados por fecha de generación descendente
-- con el id como desempate del cursor (fecha_generacion DESC, id DESC).
CREATE INDEX ix_notificaciones_bandeja
    ON notificaciones (destinatario_id, fecha_generacion DESC, id DESC);

-- Justificación del índice parcial: el resumen de la campana consulta únicamente la
-- existencia de pendientes por destinatario y el filtro por defecto de la bandeja es
-- NO_LEIDA (plan de lectura de bandeja). Con la decisión de producto de conservar los
-- avisos leídos sin purga, el histórico leído crece de forma indefinida, por lo que el
-- índice parcial mantiene el tamaño proporcional a los pendientes y sirve a la vez al
-- EXISTS del resumen y al orden de la primera página por defecto.
CREATE INDEX ix_notificaciones_bandeja_pendientes
    ON notificaciones (destinatario_id, fecha_generacion DESC, id DESC)
    WHERE leida_en IS NULL;
