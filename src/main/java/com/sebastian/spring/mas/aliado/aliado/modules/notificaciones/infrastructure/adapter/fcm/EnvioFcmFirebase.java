package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Transporte FCM sobre el SDK de Firebase Admin (F09-PUSH-09).
 *
 * <p>Convierte el mensaje propio en un {@code Message} de Firebase y espera el resultado
 * como máximo el timeout configurado (D-08: 5 s). Una espera agotada se informa como error
 * temporal: la entrega vuelve a programarse en la aplicación. El SDK se ejecuta en un hilo
 * virtual para que el timeout acote la llamada completa sin depender de los tiempos del
 * cliente HTTP. Los fallos de autenticación del SDK se informan como configuración, nunca
 * como token desregistrado.</p>
 *
 * <p>El token solo se escribe en el mensaje del proveedor; no aparece en excepciones ni en
 * logs.</p>
 */
public class EnvioFcmFirebase implements EnvioFcm, AutoCloseable {

    /** Envío síncrono al SDK; se inyecta para poder probar el timeout sin red. */
    @FunctionalInterface
    public interface EnvioSincrono {
        String enviar(Message mensaje) throws FirebaseMessagingException;
    }

    private final EnvioSincrono envio;
    private final Duration tiempoMaximo;
    private final Clock reloj;
    private final ExecutorService ejecutor;

    public EnvioFcmFirebase(EnvioSincrono envio, Duration tiempoMaximo) {
        this(envio, tiempoMaximo, Clock.systemUTC());
    }

    public EnvioFcmFirebase(EnvioSincrono envio, Duration tiempoMaximo, Clock reloj) {
        this.envio = Objects.requireNonNull(envio, "El envío a FCM es obligatorio");
        Objects.requireNonNull(tiempoMaximo, "El timeout de FCM es obligatorio");
        if (tiempoMaximo.isZero() || tiempoMaximo.isNegative()) {
            throw new IllegalArgumentException("El timeout de FCM debe ser positivo");
        }
        this.tiempoMaximo = tiempoMaximo;
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
        // Un hilo virtual por envío: el timeout corta la espera sin bloquear el pool común.
        this.ejecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public static EnvioFcmFirebase conFirebase(FirebaseMessaging mensajeria, Duration tiempoMaximo) {
        Objects.requireNonNull(mensajeria, "La mensajería de Firebase es obligatoria");
        return new EnvioFcmFirebase(mensajeria::send, tiempoMaximo);
    }

    @Override
    public String enviar(MensajeFcm mensaje) throws ExcepcionEnvioFcm {
        Message construido = aMensajeFirebase(Objects.requireNonNull(mensaje));
        Future<String> futuro = ejecutor.submit(() -> envio.enviar(construido));
        try {
            String idMensaje = futuro.get(tiempoMaximo.toMillis(), TimeUnit.MILLISECONDS);
            if (idMensaje == null || idMensaje.isBlank()) {
                throw new ExcepcionEnvioFcm(null, ErrorCode.UNKNOWN, null);
            }
            return idMensaje;
        } catch (TimeoutException agotado) {
            futuro.cancel(true);
            throw new ExcepcionEnvioFcm(null, ErrorCode.DEADLINE_EXCEEDED, null, agotado);
        } catch (InterruptedException interrumpido) {
            futuro.cancel(true);
            Thread.currentThread().interrupt();
            throw new ExcepcionEnvioFcm(null, ErrorCode.CANCELLED, null, interrumpido);
        } catch (ExecutionException fallo) {
            throw traducir(fallo.getCause());
        }
    }

    @Override
    public void close() {
        ejecutor.shutdownNow();
    }

    private ExcepcionEnvioFcm traducir(Throwable causa) {
        if (causa instanceof FirebaseMessagingException falloFirebase) {
            return new ExcepcionEnvioFcm(falloFirebase.getMessagingErrorCode(),
                    falloFirebase.getErrorCode(),
                    reintentarEn(falloFirebase), falloFirebase);
        }
        // Un mensaje o token con formato inválido se trata como petición inválida: no
        // revoca el token porque el problema puede ser del contenido.
        if (causa instanceof IllegalArgumentException) {
            return new ExcepcionEnvioFcm(MessagingErrorCode.INVALID_ARGUMENT,
                    ErrorCode.INVALID_ARGUMENT, null, causa);
        }
        // Firebase sin inicializar o credenciales no disponibles: configuración, no token.
        if (causa instanceof IllegalStateException) {
            return new ExcepcionEnvioFcm(null, ErrorCode.UNAUTHENTICATED, null, causa);
        }
        return new ExcepcionEnvioFcm(null, ErrorCode.UNKNOWN, null, causa);
    }

    private Instant reintentarEn(FirebaseMessagingException fallo) {
        Map<String, Object> cabeceras = fallo.getHttpResponse() == null
                ? Map.of()
                : fallo.getHttpResponse().getHeaders();
        return RetryAfterFcm.interpretar(cabeceras, reloj).orElse(null);
    }

    // El contrato D-08 del piloto registra el token FCM del dispositivo Android. El SDK
    // 9.12 marca setToken como obsoleto a favor de setFid (Firebase Installation ID), que
    // exigiría un cambio de contrato con Móvil; se conserva el token acordado.
    @SuppressWarnings("deprecation")
    private Message aMensajeFirebase(MensajeFcm mensaje) {
        AndroidConfig.Builder android = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(mensaje.ttl().toMillis());
        if (!mensaje.canalAndroid().isBlank()) {
            android.setNotification(AndroidNotification.builder()
                    .setChannelId(mensaje.canalAndroid())
                    .build());
        }
        return Message.builder()
                .setToken(mensaje.token())
                .setNotification(Notification.builder()
                        .setTitle(mensaje.titulo())
                        .setBody(mensaje.cuerpo())
                        .build())
                .setAndroidConfig(android.build())
                .putAllData(mensaje.datos())
                .build();
    }
}
