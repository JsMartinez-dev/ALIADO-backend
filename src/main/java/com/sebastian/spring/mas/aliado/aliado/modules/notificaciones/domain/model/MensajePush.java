package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Mensaje mínimo que el proveedor push debe entregar (F09-PUSH-01/08).
 *
 * <p>Contiene solo los identificadores de navegación acordados y el token del vínculo
 * recién revalidado. El texto visible y el resto del payload los construye el adaptador
 * del proveedor; este contrato no incluye datos de contacto ni la descripción privada de
 * la solicitud.</p>
 */
public record MensajePush(UUID notificacionId, UUID solicitudId, TipoNotificacion tipo,
                          String tokenFcm) {

    public MensajePush {
        Objects.requireNonNull(notificacionId, "El aviso del mensaje es obligatorio");
        Objects.requireNonNull(solicitudId, "La solicitud del mensaje es obligatoria");
        Objects.requireNonNull(tipo, "El tipo del mensaje es obligatorio");
        if (tokenFcm == null || tokenFcm.isBlank()) {
            throw new IllegalArgumentException("El mensaje exige el token del vínculo vigente");
        }
    }
}
