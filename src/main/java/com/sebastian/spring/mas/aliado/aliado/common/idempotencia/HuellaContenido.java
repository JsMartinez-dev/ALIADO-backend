package com.sebastian.spring.mas.aliado.aliado.common.idempotencia;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Huella estable de los campos que definen una operación idempotente. */
public final class HuellaContenido {
    private HuellaContenido() {}

    public static String calcular(String nombreOriginal, String tipoContenido, byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            agregar(digest, nombreOriginal);
            agregar(digest, tipoContenido);
            agregar(digest, contenido);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible.", ex);
        }
    }

    private static void agregar(MessageDigest digest, String valor) {
        byte[] bytes = valor == null ? new byte[0] : valor.getBytes(StandardCharsets.UTF_8);
        agregar(digest, bytes);
    }

    private static void agregar(MessageDigest digest, byte[] bytes) {
        byte[] contenido = bytes == null ? new byte[0] : bytes;
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(contenido.length).array());
        digest.update(contenido);
    }
}
