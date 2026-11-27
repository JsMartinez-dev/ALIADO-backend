-- D-05: eliminación definitiva de cuenta del MVP académico.
-- Se conserva la fila de usuario como referencia histórica anonimizada.
ALTER TABLE usuarios ALTER COLUMN email DROP NOT NULL;
ALTER TABLE usuarios ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE usuarios DROP CONSTRAINT usuarios_estado_cuenta_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_estado_cuenta_check
    CHECK (estado_cuenta IN ('ACTIVA', 'BLOQUEADA_PREVENTIVAMENTE', 'ELIMINADA'));
