-- D-05: cola durable de archivos a eliminar después del commit de la cuenta.
CREATE TABLE archivos_pendientes_eliminacion (
    clave               VARCHAR(500) PRIMARY KEY,
    fecha_creacion      TIMESTAMPTZ  NOT NULL,
    intentos            INTEGER      NOT NULL DEFAULT 0,
    ultimo_error        VARCHAR(1000),
    fecha_ultimo_intento TIMESTAMPTZ
);
CREATE INDEX ix_archivos_pendientes_fecha ON archivos_pendientes_eliminacion (fecha_creacion);
