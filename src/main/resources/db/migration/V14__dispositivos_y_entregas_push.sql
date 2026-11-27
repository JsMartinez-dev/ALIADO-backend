-- Piloto Android. La bandeja y la idempotencia de notificaciones siguen en V4.
CREATE TABLE instalaciones_push (
    id UUID PRIMARY KEY,
    plataforma VARCHAR(16) NOT NULL CHECK (plataforma = 'ANDROID'),
    prueba_sha256 BYTEA NOT NULL CHECK (octet_length(prueba_sha256) = 32),
    creada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cada reactivación, traslado o rotación abre una generación. Se conservan las
-- huellas retiradas para impedir que una credencial antigua vuelva a ser válida.
CREATE TABLE vinculos_push (
    instalacion_id UUID NOT NULL REFERENCES instalaciones_push (id),
    generacion BIGINT NOT NULL CHECK (generacion > 0),
    usuario_id UUID NOT NULL REFERENCES usuarios (id),
    token_fcm VARCHAR(4096),
    token_fcm_sha256 BYTEA,
    revocacion_sha256 BYTEA NOT NULL CHECK (octet_length(revocacion_sha256) = 32),
    vinculado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revocado_en TIMESTAMPTZ,
    PRIMARY KEY (instalacion_id, generacion),
    CONSTRAINT uq_vinculo_push_revocacion UNIQUE (revocacion_sha256),
    CONSTRAINT ck_vinculo_push_token_activo CHECK (
        (revocado_en IS NULL AND token_fcm IS NOT NULL AND length(btrim(token_fcm)) > 0
            AND token_fcm_sha256 IS NOT NULL
            AND token_fcm_sha256 = sha256(convert_to(token_fcm, 'UTF8')))
        OR (revocado_en IS NOT NULL AND token_fcm IS NULL AND token_fcm_sha256 IS NULL)
    )
);

CREATE UNIQUE INDEX uq_vinculo_push_instalacion_activa
    ON vinculos_push (instalacion_id) WHERE revocado_en IS NULL;
CREATE UNIQUE INDEX uq_vinculo_push_token_activo
    ON vinculos_push (token_fcm_sha256) WHERE revocado_en IS NULL;
CREATE INDEX ix_vinculo_push_usuario_activo
    ON vinculos_push (usuario_id) WHERE revocado_en IS NULL;

CREATE TABLE trabajos_push (
    id UUID PRIMARY KEY,
    notificacion_id UUID NOT NULL REFERENCES notificaciones (id) ON DELETE CASCADE,
    canal VARCHAR(16) NOT NULL CHECK (canal = 'PUSH'),
    estado VARCHAR(24) NOT NULL CHECK (estado IN ('PENDIENTE_PREPARACION', 'PREPARADO', 'FINALIZADO')),
    encolado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en TIMESTAMPTZ NOT NULL CHECK (expira_en > encolado_en),
    reclamacion_id UUID,
    reclamacion_vence_en TIMESTAMPTZ,
    finalizado_en TIMESTAMPTZ,
    motivo_final VARCHAR(64),
    CONSTRAINT uq_trabajo_push_notificacion_canal UNIQUE (notificacion_id, canal),
    CONSTRAINT ck_trabajo_push_reclamacion CHECK (
        (reclamacion_id IS NULL AND reclamacion_vence_en IS NULL)
        OR (reclamacion_id IS NOT NULL AND reclamacion_vence_en IS NOT NULL
            AND estado = 'PENDIENTE_PREPARACION')
    ),
    CONSTRAINT ck_trabajo_push_finalizacion CHECK (
        (estado = 'FINALIZADO' AND finalizado_en IS NOT NULL AND motivo_final IS NOT NULL)
        OR (estado <> 'FINALIZADO' AND finalizado_en IS NULL AND motivo_final IS NULL)
    )
);

CREATE INDEX ix_trabajo_push_preparacion
    ON trabajos_push (encolado_en, id) WHERE estado = 'PENDIENTE_PREPARACION';
CREATE INDEX ix_trabajo_push_reclamacion
    ON trabajos_push (reclamacion_vence_en) WHERE reclamacion_vence_en IS NOT NULL;
CREATE INDEX ix_trabajo_push_expiracion
    ON trabajos_push (expira_en) WHERE estado <> 'FINALIZADO';

CREATE TABLE entregas_push (
    id UUID PRIMARY KEY,
    trabajo_id UUID NOT NULL REFERENCES trabajos_push (id) ON DELETE CASCADE,
    instalacion_id UUID NOT NULL,
    generacion BIGINT NOT NULL,
    estado VARCHAR(24) NOT NULL CHECK (
        estado IN ('PENDIENTE', 'ACEPTADA', 'FALLIDA_DEFINITIVA', 'SUPRIMIDA', 'EXPIRADA')
    ),
    intentos INTEGER NOT NULL DEFAULT 0 CHECK (intentos >= 0),
    proximo_intento_en TIMESTAMPTZ,
    reclamacion_id UUID,
    reclamacion_vence_en TIMESTAMPTZ,
    id_mensaje_fcm VARCHAR(512),
    ultimo_codigo_error VARCHAR(64),
    finalizada_en TIMESTAMPTZ,
    CONSTRAINT fk_entrega_push_vinculo
        FOREIGN KEY (instalacion_id, generacion) REFERENCES vinculos_push (instalacion_id, generacion),
    CONSTRAINT uq_entrega_push_generacion UNIQUE (trabajo_id, instalacion_id, generacion),
    CONSTRAINT ck_entrega_push_reclamacion CHECK (
        (reclamacion_id IS NULL AND reclamacion_vence_en IS NULL)
        OR (reclamacion_id IS NOT NULL AND reclamacion_vence_en IS NOT NULL AND estado = 'PENDIENTE')
    ),
    CONSTRAINT ck_entrega_push_plazo CHECK (
        (estado = 'PENDIENTE' AND proximo_intento_en IS NOT NULL AND finalizada_en IS NULL)
        OR (estado <> 'PENDIENTE' AND proximo_intento_en IS NULL AND finalizada_en IS NOT NULL)
    ),
    CONSTRAINT ck_entrega_push_mensaje CHECK (
        (estado = 'ACEPTADA' AND id_mensaje_fcm IS NOT NULL)
        OR (estado <> 'ACEPTADA' AND id_mensaje_fcm IS NULL)
    )
);

CREATE INDEX ix_entrega_push_proximo_intento
    ON entregas_push (proximo_intento_en, id) WHERE estado = 'PENDIENTE';
CREATE INDEX ix_entrega_push_reclamacion
    ON entregas_push (reclamacion_vence_en) WHERE reclamacion_vence_en IS NOT NULL;
CREATE INDEX ix_entrega_push_vinculo ON entregas_push (instalacion_id, generacion);
