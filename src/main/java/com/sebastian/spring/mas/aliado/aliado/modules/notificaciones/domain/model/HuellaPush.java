package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Huella SHA-256 de un secreto de instalación (F09-PUSH-01/D-08).
 * Los secretos nunca se persisten ni se comparan en claro: solo su huella,
 * comparada en tiempo constante.
 */
public final class HuellaPush {
    private final byte[] valor;

    private HuellaPush(byte[] valor) {
        this.valor = valor.clone();
    }

    public static HuellaPush de(String secreto) {
        if (secreto == null) {
            throw new IllegalArgumentException("El secreto de la instalación es obligatorio.");
        }
        try {
            return new HuellaPush(MessageDigest.getInstance("SHA-256")
                    .digest(secreto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible.", ex);
        }
    }

    public byte[] valor() {
        return valor.clone();
    }

    public boolean coincide(byte[] otra) {
        return otra != null && MessageDigest.isEqual(valor, otra);
    }
}
