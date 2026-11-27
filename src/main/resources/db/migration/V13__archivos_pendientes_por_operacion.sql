-- D-05: asocia cada archivo pendiente a la operación de eliminación que lo originó.
-- La columna queda nullable para no romper filas históricas creadas por V12; el código
-- nuevo siempre escribe operacion_id y cuenta por operación.
ALTER TABLE archivos_pendientes_eliminacion
    ADD COLUMN operacion_id UUID REFERENCES solicitudes_eliminacion (id);
CREATE INDEX ix_archivos_pendientes_operacion
    ON archivos_pendientes_eliminacion (operacion_id);
