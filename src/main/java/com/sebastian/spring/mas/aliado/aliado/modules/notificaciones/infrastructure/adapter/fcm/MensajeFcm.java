package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Mensaje ya resuelto para Firebase Cloud Messaging (F09-PUSH-09).
 *
 * <p>Es el límite entre el contrato del núcleo y el SDK del proveedor: el adaptador recibe
 * un {@code MensajePush} con identificadores, lo traduce a este tipo con el título, el
 * texto y el bloque de datos visibles, y solo la implementación de transporte lo convierte
 * en un {@code Message} de Firebase. Mantener este tipo propio permite comprobar el
 * payload sin exponer el SDK en las pruebas.</p>
 *
 * <p>El token es necesario para entregar el mensaje, pero no forma parte del contenido
 * visible ni del bloque de datos y no debe registrarse en logs.</p>
 */
public record MensajeFcm(String token, String titulo, String cuerpo,
                         Map<String, String> datos, Duration ttl, String canalAndroid) {

    public MensajeFcm {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El mensaje FCM exige el token del vínculo vigente");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El mensaje FCM exige un título");
        }
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new IllegalArgumentException("El mensaje FCM exige un texto");
        }
        datos = Map.copyOf(Objects.requireNonNull(datos, "Los datos del mensaje FCM son obligatorios"));
        if (ttl == null || ttl.isNegative()) {
            throw new IllegalArgumentException("El TTL del mensaje FCM no puede ser negativo");
        }
        canalAndroid = canalAndroid == null ? "" : canalAndroid.trim();
    }
}
