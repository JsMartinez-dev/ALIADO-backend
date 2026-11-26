package com.sebastian.spring.mas.aliado.aliado.common.storage;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import java.util.Set;

public final class ArchivoValidacion {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final Set<String> IMAGENES = Set.of("image/jpeg", "image/png", "image/webp");
    public static final Set<String> DOCUMENTOS = Set.of("application/pdf", "image/jpeg", "image/png", "image/webp");

    private ArchivoValidacion() {}

    public static void validar(String nombreOriginal, String tipoContenido, byte[] contenido, Set<String> permitidos) {
        if (contenido == null || contenido.length == 0) {
            throw new BusinessException.BadRequest("ARCHIVO_VACIO", "El archivo está vacío.");
        }
        if (contenido.length > MAX_BYTES) {
            throw new BusinessException.PayloadTooLarge("ARCHIVO_MUY_GRANDE", "El archivo supera los 5 MB permitidos.");
        }
        if (tipoContenido == null || !permitidos.contains(tipoContenido.toLowerCase())) {
            throw new BusinessException.UnsupportedMediaType("TIPO_ARCHIVO_NO_PERMITIDO",
                    "Tipo de archivo no permitido: " + tipoContenido);
        }
        // La cabecera Content-Type la declara el cliente: verificar la firma (magic bytes)
        // para que un ejecutable renombrado no pase como imagen o PDF.
        if (!firmaCorresponde(tipoContenido.toLowerCase(), contenido)) {
            throw new BusinessException.UnsupportedMediaType("TIPO_ARCHIVO_NO_PERMITIDO",
                    "El contenido del archivo no corresponde al tipo declarado: " + tipoContenido);
        }
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new BusinessException.BadRequest("ARCHIVO_SIN_NOMBRE", "El archivo debe tener un nombre.");
        }
    }

    /** Firma (magic bytes) esperada para cada tipo permitido. */
    static boolean firmaCorresponde(String tipoContenido, byte[] contenido) {
        return switch (tipoContenido) {
            case "image/jpeg" -> empiezaCon(contenido, 0xFF, 0xD8, 0xFF);
            case "image/png" -> empiezaCon(contenido, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/webp" -> contenido.length >= 12
                    && empiezaCon(contenido, 'R', 'I', 'F', 'F')
                    && contenido[8] == 'W' && contenido[9] == 'E' && contenido[10] == 'B' && contenido[11] == 'P';
            case "application/pdf" -> empiezaCon(contenido, '%', 'P', 'D', 'F', '-');
            default -> false;
        };
    }

    private static boolean empiezaCon(byte[] contenido, int... firma) {
        if (contenido.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((contenido[i] & 0xFF) != (firma[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    /** Detecta el tipo real del contenido por su firma; null si no es un tipo permitido. */
    public static String detectarTipo(byte[] contenido) {
        if (contenido == null) {
            return null;
        }
        if (empiezaCon(contenido, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (empiezaCon(contenido, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (contenido.length >= 12 && empiezaCon(contenido, 'R', 'I', 'F', 'F')
                && contenido[8] == 'W' && contenido[9] == 'E' && contenido[10] == 'B' && contenido[11] == 'P') {
            return "image/webp";
        }
        if (empiezaCon(contenido, '%', 'P', 'D', 'F', '-')) {
            return "application/pdf";
        }
        return null;
    }

    public static String extensionSegura(String nombreOriginal) {
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto < 0 || punto == nombreOriginal.length() - 1) {
            return "";
        }
        String ext = nombreOriginal.substring(punto).toLowerCase();
        return ext.matches("\\.[a-z0-9]+") ? ext : "";
    }
}
