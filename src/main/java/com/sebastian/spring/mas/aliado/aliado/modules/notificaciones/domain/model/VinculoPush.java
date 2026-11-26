package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Vínculo activo o retirado entre una instalación Android y una cuenta (F09-PUSH-01/D-08).
 * Cada reactivación, cambio de cuenta o rotación del token abre una generación nueva;
 * las generaciones retiradas se conservan para impedir que una credencial antigua vuelva
 * a habilitar un vínculo.
 */
public record VinculoPush(UUID instalacionId, long generacion, UUID usuarioId, String tokenFcm,
                          byte[] tokenFcmSha256, byte[] revocacionSha256,
                          OffsetDateTime vinculadoEn, OffsetDateTime revocadoEn) {

    public boolean activo() {
        return revocadoEn == null;
    }

    public boolean esDe(UUID usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    public boolean usaToken(byte[] huellaToken) {
        return tokenFcmSha256 != null && huellaToken != null
                && MessageDigest.isEqual(tokenFcmSha256, huellaToken);
    }

    public boolean coincideCredencial(byte[] huellaCredencial) {
        return revocacionSha256 != null && huellaCredencial != null
                && MessageDigest.isEqual(revocacionSha256, huellaCredencial);
    }
}
