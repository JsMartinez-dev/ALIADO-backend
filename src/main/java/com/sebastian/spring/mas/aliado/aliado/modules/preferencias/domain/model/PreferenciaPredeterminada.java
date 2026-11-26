package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model;

/**
 * Valor de una preferencia cuando no existe elección persistida para ese par (F09-PUSH-01, D-08).
 *
 * <p>El piloto Android arranca apagado: sin fila en {@code preferencias_notificacion} para
 * {@code NUEVA_OPORTUNIDAD/PUSH}, la preferencia vale {@code false} y solo se persiste el aviso
 * de bandeja. Las elecciones explícitas siempre prevalecen y el resto de pares conserva el
 * comportamiento anterior (habilitado por defecto), igual que el aviso de seguridad de cuenta,
 * que no es configurable.</p>
 *
 * <p>La misma regla alimenta la consulta de encolado, la revalidación previa al envío y el
 * listado de preferencias, para que las tres lecturas devuelvan el mismo valor.</p>
 */
public final class PreferenciaPredeterminada {
    private static final String CANAL_PUSH = "PUSH";

    private PreferenciaPredeterminada() {
    }

    public static boolean habilitada(TipoAviso tipoAviso, String canal) {
        if (tipoAviso == TipoAviso.AVISO_SEGURIDAD_CUENTA) {
            return true;
        }
        return !(tipoAviso == TipoAviso.NUEVA_OPORTUNIDAD && CANAL_PUSH.equals(canal));
    }
}
