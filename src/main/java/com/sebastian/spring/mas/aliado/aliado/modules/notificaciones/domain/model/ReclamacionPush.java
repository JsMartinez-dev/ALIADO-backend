package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Reclamación vigente de un trabajo padre o de una entrega por dispositivo
 * (F09-PUSH-07, V9).
 *
 * <p>El identificador y el vencimiento los escribe el adaptador de PostgreSQL en la misma
 * transacción corta que reclama la fila y emite {@code NOTIFY}. El vencimiento es el
 * plazo con el que otra instancia puede volver a reclamar la fila si el worker actual
 * cae antes de guardar el resultado. Las escrituras de resultado se condicionan a este
 * identificador y a que el vencimiento siga en el futuro.</p>
 *
 * <p>Una reclamación de preparación identifica solo el trabajo; una de envío identifica
 * además la entrega, la instalación y la generación del token usadas al reclamar.</p>
 */
public record ReclamacionPush(TipoReclamacionPush tipo, UUID trabajoId, UUID entregaId,
                              UUID instalacionId, Long generacion, UUID reclamacionId,
                              Instant venceEn) {

    public ReclamacionPush {
        Objects.requireNonNull(tipo, "El tipo de reclamación es obligatorio");
        Objects.requireNonNull(trabajoId, "El trabajo de la reclamación es obligatorio");
        Objects.requireNonNull(reclamacionId, "El identificador de reclamación es obligatorio");
        Objects.requireNonNull(venceEn, "El vencimiento de la reclamación es obligatorio");
        if (tipo == TipoReclamacionPush.PREPARACION) {
            if (entregaId != null || instalacionId != null || generacion != null) {
                throw new IllegalArgumentException(
                        "Una reclamación de preparación no identifica entrega ni dispositivo");
            }
        } else if (entregaId == null || instalacionId == null || generacion == null || generacion < 1) {
            throw new IllegalArgumentException(
                    "Una reclamación de envío exige entrega, instalación y generación");
        }
    }

    public static ReclamacionPush preparacion(UUID trabajoId, UUID reclamacionId, Instant venceEn) {
        return new ReclamacionPush(TipoReclamacionPush.PREPARACION, trabajoId, null, null, null,
                reclamacionId, venceEn);
    }

    public static ReclamacionPush envio(UUID entregaId, UUID trabajoId, UUID instalacionId,
                                        long generacion, UUID reclamacionId, Instant venceEn) {
        return new ReclamacionPush(TipoReclamacionPush.ENVIO, trabajoId, entregaId, instalacionId,
                generacion, reclamacionId, venceEn);
    }
}
