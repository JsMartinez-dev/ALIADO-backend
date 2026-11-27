package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Traduce el mensaje mínimo del núcleo al payload de Firebase (F09-PUSH-09).
 *
 * <p>El título y el texto son los genéricos acordados en F09-PUSH-01 y no incluyen
 * dirección, contacto ni la descripción de la solicitud. El bloque de datos lleva
 * únicamente los tres identificadores de navegación: {@code tipo},
 * {@code notificacionId} y {@code solicitudId}.</p>
 */
public class MensajeFcmMapper {

    public static final String TITULO = "Nueva oportunidad";
    public static final String CUERPO = "Tienes una oportunidad compatible en ALIADO";

    private final Duration ttl;
    private final String canalAndroid;

    public MensajeFcmMapper(Duration ttl, String canalAndroid) {
        Objects.requireNonNull(ttl, "El TTL del mensaje es obligatorio");
        if (ttl.isNegative()) {
            throw new IllegalArgumentException("El TTL del mensaje no puede ser negativo");
        }
        this.ttl = ttl;
        this.canalAndroid = canalAndroid == null ? "" : canalAndroid.trim();
    }

    public MensajeFcm aMensaje(MensajePush mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje push es obligatorio");
        return new MensajeFcm(mensaje.tokenFcm(), TITULO, CUERPO,
                Map.of("tipo", mensaje.tipo().name(),
                        "notificacionId", mensaje.notificacionId().toString(),
                        "solicitudId", mensaje.solicitudId().toString()),
                ttl, canalAndroid);
    }
}
