package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Instalación Android registrada (F09-PUSH-01/D-08).
 * La prueba de posesión es un secreto de alta generado por el dispositivo;
 * el backend conserva únicamente su huella.
 */
public record InstalacionPush(UUID id, String plataforma, byte[] pruebaSha256, OffsetDateTime creadaEn) {

    public boolean coincidePrueba(byte[] huella) {
        return pruebaSha256 != null && huella != null && MessageDigest.isEqual(pruebaSha256, huella);
    }
}
