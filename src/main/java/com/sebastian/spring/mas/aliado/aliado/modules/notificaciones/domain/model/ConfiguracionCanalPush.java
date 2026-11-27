package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Activación del canal push por entorno y tipo (F09-PUSH-11, D-08).
 *
 * <p>El piloto solo habilita {@link TipoNotificacion#NUEVA_OPORTUNIDAD}. El interruptor
 * {@code entregaHabilitada} permite apagar el envío sin retirar la bandeja: el aviso se
 * sigue registrando y consultando, pero no se crea trabajo nuevo ni se contacta al
 * proveedor. El apagado no reprocesa avisos históricos: solo afecta al encolado que
 * ocurre después de la publicación.</p>
 */
public record ConfiguracionCanalPush(boolean entregaHabilitada, Set<TipoNotificacion> tiposHabilitados) {

    public ConfiguracionCanalPush {
        Objects.requireNonNull(tiposHabilitados, "Los tipos habilitados son obligatorios");
        tiposHabilitados = Set.copyOf(tiposHabilitados);
    }

    /**
     * Construye la configuración desde una lista separada por comas. Un nombre
     * desconocido falla al componer para que una errata no apague el canal en silencio.
     */
    public static ConfiguracionCanalPush desde(boolean entregaHabilitada, String tiposSeparadosPorComa) {
        Set<TipoNotificacion> tipos = new LinkedHashSet<>();
        String lista = tiposSeparadosPorComa == null ? "" : tiposSeparadosPorComa;
        for (String valor : Arrays.stream(lista.split(","))
                .map(String::trim)
                .filter(texto -> !texto.isEmpty())
                .toList()) {
            try {
                tipos.add(TipoNotificacion.valueOf(valor));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Tipo de notificación push desconocido en aliado.push.tipos-habilitados: " + valor, ex);
            }
        }
        return new ConfiguracionCanalPush(entregaHabilitada, tipos);
    }

    /** True solo si el interruptor global está encendido y el tipo figura como habilitado. */
    public boolean habilitadoPara(TipoNotificacion tipo) {
        return entregaHabilitada && tipo != null && tiposHabilitados.contains(tipo);
    }
}
