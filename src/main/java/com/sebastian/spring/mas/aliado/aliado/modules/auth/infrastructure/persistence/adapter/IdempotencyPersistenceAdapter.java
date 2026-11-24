package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotencyRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.entity.IdempotencyKeyId;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.persistence.jpa.IdempotencyJpaRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyPersistenceAdapter implements IdempotencyRepository {
    private final IdempotencyJpaRepository jpa;

    public IdempotencyPersistenceAdapter(IdempotencyJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<RespuestaGuardada> buscar(UUID clave, String operacion) {
        return jpa.findById(new IdempotencyKeyId(clave, operacion))
                .filter(e -> e.getFechaExpiracion().isAfter(OffsetDateTime.now()))
                .map(e -> new RespuestaGuardada(e.getStatusCode(), e.getRespuesta()));
    }

    @Override
    public Optional<RespuestaConHuella> buscarConHuella(UUID clave, String operacion) {
        return jpa.findById(new IdempotencyKeyId(clave, operacion))
                .filter(e -> e.getFechaExpiracion().isAfter(OffsetDateTime.now()))
                .map(e -> new RespuestaConHuella(e.getStatusCode(), e.getRespuesta(), e.getHuella()));
    }

    @Override
    public void guardar(UUID clave, String operacion, UUID usuarioId, int statusCode, String cuerpoJson) {
        guardarConHuella(clave, operacion, usuarioId, statusCode, cuerpoJson, null);
    }

    @Override
    public void guardarConHuella(UUID clave, String operacion, UUID usuarioId, int statusCode,
                                 String cuerpoJson, String huella) {
        try {
            OffsetDateTime ahora = OffsetDateTime.now();
            IdempotencyKeyEntity e = new IdempotencyKeyEntity();
            e.setId(new IdempotencyKeyId(clave, operacion));
            e.setUsuarioId(usuarioId);
            e.setStatusCode(statusCode);
            e.setRespuesta(cuerpoJson);
            e.setHuella(huella);
            e.setFechaCreacion(ahora);
            e.setFechaExpiracion(ahora.plusHours(24));
            jpa.save(e);
        } catch (DataIntegrityViolationException ignored) {
            // Otro reintento concurrente ya guardó la respuesta; se conserva la primera.
        }
    }

    @Override
    public void bloquear(UUID clave, String operacion) {
        jpa.bloquear(clave, operacion);
    }
}
