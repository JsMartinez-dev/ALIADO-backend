package com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Sesión opaca: el token en claro nunca se persiste, solo su hash SHA-256 (SPEC-01). */
public record Sesion(UUID id, UUID usuarioId, String tokenHash, OffsetDateTime fechaInicio,
                     OffsetDateTime fechaExpiracion, OffsetDateTime fechaCierre, EstadoSesion estado) {

    public boolean vigente(OffsetDateTime ahora) {
        return estado == EstadoSesion.ACTIVA && fechaExpiracion.isAfter(ahora);
    }

    public Sesion cerrada(OffsetDateTime ahora) {
        return new Sesion(id, usuarioId, tokenHash, fechaInicio, fechaExpiracion, ahora, EstadoSesion.CERRADA);
    }


    public Sesion expirada() {
        return new Sesion(id, usuarioId, tokenHash, fechaInicio, fechaExpiracion, fechaCierre, EstadoSesion.EXPIRADA);
    }
}
