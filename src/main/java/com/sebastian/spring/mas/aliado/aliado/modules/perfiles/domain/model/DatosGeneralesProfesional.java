package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contrato mínimo de datos generales del MVP (BE-05).
 * Solo la presentación libre del Prestador; los demás apartados de F05 tienen su propia estructura.
 */
public final class DatosGeneralesProfesional {
    public static final String DESCRIPCION = "descripcion";
    public static final int MAX_DESCRIPCION = 1000;

    private DatosGeneralesProfesional() {}

    public static String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        String normalizada = descripcion.trim();
        if (normalizada.length() > MAX_DESCRIPCION) {
            throw new BusinessException.BadRequest("DATOS_GENERALES_INVALIDOS",
                    "descripcion admite máximo " + MAX_DESCRIPCION + " caracteres.");
        }
        return normalizada;
    }

    public static Map<String, Object> paraCreacion(String descripcion) {
        String normalizada = normalizarDescripcion(descripcion);
        return normalizada == null ? Map.of() : Map.of(DESCRIPCION, normalizada);
    }

    /**
     * Fusiona el campo editable con el mapa existente para no perder claves heredadas
     * que no forman parte del contrato MVP.
     */
    public static Map<String, Object> fusionar(Map<String, Object> actuales, String descripcion) {
        Map<String, Object> resultado = actuales == null ? new LinkedHashMap<>() : new LinkedHashMap<>(actuales);
        String normalizada = normalizarDescripcion(descripcion);
        if (normalizada == null) {
            resultado.remove(DESCRIPCION);
        } else {
            resultado.put(DESCRIPCION, normalizada);
        }
        return Collections.unmodifiableMap(resultado);
    }

    public static String descripcionDe(Map<String, Object> datos) {
        if (datos == null) {
            return null;
        }
        Object valor = datos.get(DESCRIPCION);
        return valor instanceof String texto && !texto.isBlank() ? texto : null;
    }
}
