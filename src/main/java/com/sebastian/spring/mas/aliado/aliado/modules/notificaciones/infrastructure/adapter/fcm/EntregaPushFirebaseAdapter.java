package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.MessagingErrorCode;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptador de entrega push con Firebase Cloud Messaging (F09-PUSH-09).
 *
 * <p>Implementa {@link EntregaPushPort}: revalida el tipo habilitado del piloto, construye
 * el payload mínimo, llama al transporte FCM y clasifica la respuesta del proveedor en
 * aceptación, error temporal, error permanente o token desregistrado. La política de
 * reintentos, el cierre del padre y la revocación de la generación siguen en la
 * aplicación (F09-PUSH-08); este adaptador no decide plazos.</p>
 *
 * <p>Un payload inválido, un error de configuración o unas credenciales incorrectas se
 * clasifican como permanentes y conservan el token: solo {@code UNREGISTERED} revoca la
 * generación enviada. Ni los logs ni los códigos persistidos incluyen el token del
 * dispositivo.</p>
 */
public class EntregaPushFirebaseAdapter implements EntregaPushPort {

    private static final Logger log = LoggerFactory.getLogger(EntregaPushFirebaseAdapter.class);

    private final EnvioFcm envio;
    private final MensajeFcmMapper mapper;

    public EntregaPushFirebaseAdapter(EnvioFcm envio, MensajeFcmMapper mapper) {
        this.envio = Objects.requireNonNull(envio, "El transporte FCM es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "El mapeo del mensaje FCM es obligatorio");
    }

    @Override
    public ResultadoEnvioPush enviar(MensajePush mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje push es obligatorio");
        if (mensaje.tipo() != TipoNotificacion.NUEVA_OPORTUNIDAD) {
            // El piloto habilita FCM solo para este tipo; otro aviso se cierra como
            // permanente sin contactar al proveedor.
            log.warn("Envío FCM omitido por tipo no habilitado tipo={}", mensaje.tipo());
            return ResultadoEnvioPush.permanente(CodigosEntregaPush.TIPO_NO_SOPORTADO);
        }
        try {
            String idMensaje = envio.enviar(mapper.aMensaje(mensaje));
            if (idMensaje == null || idMensaje.isBlank()) {
                return ResultadoEnvioPush.temporal(CodigosEntregaPush.PROVEEDOR_TEMPORAL, null);
            }
            log.info("FCM aceptó el envío notificacionId={} tipo={}",
                    mensaje.notificacionId(), mensaje.tipo());
            return ResultadoEnvioPush.aceptado(idMensaje);
        } catch (ExcepcionEnvioFcm fallo) {
            ResultadoEnvioPush resultado = clasificar(fallo);
            log.warn("FCM no aceptó el envío notificacionId={} tipo={} resultado={} codigo={}",
                    mensaje.notificacionId(), mensaje.tipo(), resultado.tipo(), resultado.codigo());
            return resultado;
        } catch (IllegalArgumentException mensajeInvalido) {
            log.warn("Mensaje FCM inválido notificacionId={} tipo={}",
                    mensaje.notificacionId(), mensaje.tipo());
            return ResultadoEnvioPush.permanente("FCM_INVALID_ARGUMENT");
        }
    }

    private ResultadoEnvioPush clasificar(ExcepcionEnvioFcm fallo) {
        MessagingErrorCode mensajeria = fallo.codigoMensajeria();
        if (mensajeria != null) {
            return switch (mensajeria) {
                case UNREGISTERED -> ResultadoEnvioPush.tokenDesregistrado(
                        CodigosEntregaPush.TOKEN_DESREGISTRADO);
                case QUOTA_EXCEEDED, UNAVAILABLE, INTERNAL -> ResultadoEnvioPush.temporal(
                        codigoProveedor(mensajeria), fallo.reintentarEn());
                // Un payload inválido o un remitente que no corresponde al token no
                // autorizan a revocar el vínculo: pueden deberse al contenido o a la
                // configuración del proyecto.
                case INVALID_ARGUMENT, SENDER_ID_MISMATCH, THIRD_PARTY_AUTH_ERROR ->
                        ResultadoEnvioPush.permanente(codigoProveedor(mensajeria));
            };
        }

        ErrorCode general = fallo.codigoGeneral();
        if (general != null) {
            return switch (general) {
                // Credenciales o permisos del backend: fallo de configuración. El token
                // del dispositivo sigue siendo válido.
                case UNAUTHENTICATED, PERMISSION_DENIED -> ResultadoEnvioPush.permanente(
                        CodigosEntregaPush.PROVEEDOR_NO_CONFIGURADO);
                case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE, NOT_FOUND,
                        CONFLICT, ALREADY_EXISTS, DATA_LOSS ->
                        ResultadoEnvioPush.permanente(codigoProveedor(general));
                case UNAVAILABLE, DEADLINE_EXCEEDED, RESOURCE_EXHAUSTED, INTERNAL,
                        ABORTED, CANCELLED, UNKNOWN ->
                        ResultadoEnvioPush.temporal(codigoProveedor(general), fallo.reintentarEn());
            };
        }

        return ResultadoEnvioPush.temporal(CodigosEntregaPush.PROVEEDOR_TEMPORAL,
                fallo.reintentarEn());
    }

    /** Código corto y estable del proveedor; cabe en la columna de último error. */
    private static String codigoProveedor(Enum<?> codigo) {
        return "FCM_" + codigo.name();
    }
}
