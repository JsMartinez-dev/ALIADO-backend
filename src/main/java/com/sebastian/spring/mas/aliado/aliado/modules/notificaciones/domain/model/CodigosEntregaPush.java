package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

/**
 * Códigos de error propios de la entrega push (F09-PUSH-08).
 *
 * <p>Se guardan en {@code entregas_push.ultimo_codigo_error} junto a los códigos cortos
 * que aporte el proveedor. No contienen tokens ni datos personales.</p>
 */
public final class CodigosEntregaPush {

    public static final String PREFERENCIA_DESHABILITADA = "PREFERENCIA_DESHABILITADA";
    public static final String OPORTUNIDAD_NO_VIGENTE = "OPORTUNIDAD_NO_VIGENTE";
    public static final String VINCULO_REVOCADO = "VINCULO_REVOCADO";
    public static final String GENERACION_OBSOLETA = "GENERACION_OBSOLETA";
    public static final String TIPO_NO_SOPORTADO = "TIPO_NO_SOPORTADO";
    public static final String CANAL_NO_HABILITADO = "CANAL_NO_HABILITADO";
    public static final String VIGENCIA_AGOTADA = "VIGENCIA_AGOTADA";
    public static final String INTENTOS_AGOTADOS = "INTENTOS_AGOTADOS";
    public static final String TOKEN_DESREGISTRADO = "TOKEN_DESREGISTRADO";
    public static final String PROVEEDOR_TEMPORAL = "PROVEEDOR_TEMPORAL";
    public static final String PROVEEDOR_PERMANENTE = "PROVEEDOR_PERMANENTE";
    public static final String PROVEEDOR_NO_CONFIGURADO = "PROVEEDOR_NO_CONFIGURADO";

    private CodigosEntregaPush() {
    }

    /** Limita el código al tamaño de la columna y evita nulos. */
    public static String normalizar(String codigo, String porDefecto) {
        String limpio = codigo == null ? "" : codigo.trim();
        if (limpio.isEmpty()) {
            limpio = porDefecto;
        }
        return limpio.length() <= 64 ? limpio : limpio.substring(0, 64);
    }
}
