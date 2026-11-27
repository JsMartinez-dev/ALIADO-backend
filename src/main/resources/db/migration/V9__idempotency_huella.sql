-- BE-13: huella estable del contenido para detectar reutilización de Idempotency-Key
ALTER TABLE idempotency_keys ADD COLUMN huella VARCHAR(64);
