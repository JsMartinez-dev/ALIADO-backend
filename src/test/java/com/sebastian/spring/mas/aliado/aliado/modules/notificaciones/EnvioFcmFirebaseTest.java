package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.MessagingErrorCode;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EnvioFcmFirebase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.ExcepcionEnvioFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.MensajeFcm;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-09: el transporte FCM acota la llamada al timeout acordado y traduce las
 * excepciones del SDK a códigos propios sin incluir el token.
 */
class EnvioFcmFirebaseTest {

    private static final MensajeFcm MENSAJE = new MensajeFcm("token-fcm-de-prueba",
            "Nueva oportunidad", "Tienes una oportunidad compatible en ALIADO",
            Map.of("tipo", "NUEVA_OPORTUNIDAD"), Duration.ofSeconds(3_600), "aliado_oportunidades");

    @Test
    void devuelveElIdentificadorQueAsignaElProveedor() throws Exception {
        try (EnvioFcmFirebase envio = new EnvioFcmFirebase(mensaje -> "projects/aliado/messages/1",
                Duration.ofSeconds(1))) {
            assertEquals("projects/aliado/messages/1", envio.enviar(MENSAJE));
        }
    }

    @Test
    void unTimeoutSeInformaComoPlazoAgotadoSinEsperarAlProveedor() {
        try (EnvioFcmFirebase envio = new EnvioFcmFirebase(mensaje -> {
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
            return "demasiado-tarde";
        }, Duration.ofMillis(50))) {
            long inicio = System.nanoTime();

            ExcepcionEnvioFcm fallo = assertThrows(ExcepcionEnvioFcm.class,
                    () -> envio.enviar(MENSAJE));

            long transcurrido = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
            assertEquals(ErrorCode.DEADLINE_EXCEEDED, fallo.codigoGeneral());
            assertTrue(transcurrido < 1_500, "el timeout acota la llamada completa: " + transcurrido);
            assertNotNull(fallo.getMessage());
            assertFalse(fallo.getMessage().contains(MENSAJE.token()),
                    "el mensaje del fallo no reproduce el token");
        }
    }

    @Test
    void unMensajeInvalidoSeTraduceComoPeticionInvalidaSinRevocarElToken() {
        try (EnvioFcmFirebase envio = new EnvioFcmFirebase(mensaje -> {
            throw new IllegalArgumentException("token con formato inválido");
        }, Duration.ofSeconds(1))) {
            ExcepcionEnvioFcm fallo = assertThrows(ExcepcionEnvioFcm.class,
                    () -> envio.enviar(MENSAJE));

            assertEquals(MessagingErrorCode.INVALID_ARGUMENT, fallo.codigoMensajeria());
        }
    }

    @Test
    void unSdkSinInicializarSeTraduceComoCredencialesNoDisponibles() {
        try (EnvioFcmFirebase envio = new EnvioFcmFirebase(mensaje -> {
            throw new IllegalStateException("FirebaseApp no inicializado");
        }, Duration.ofSeconds(1))) {
            ExcepcionEnvioFcm fallo = assertThrows(ExcepcionEnvioFcm.class,
                    () -> envio.enviar(MENSAJE));

            assertEquals(ErrorCode.UNAUTHENTICATED, fallo.codigoGeneral());
            assertTrue(fallo.codigoMensajeria() == null);
        }
    }

    @Test
    void elConstructorRechazaUnTimeoutNoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> new EnvioFcmFirebase(mensaje -> "id", Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new EnvioFcmFirebase(mensaje -> "id", Duration.ofSeconds(-1)));
    }
}
