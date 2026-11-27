package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.MessagingErrorCode;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EntregaPushFirebaseAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EnvioFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.ExcepcionEnvioFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.MensajeFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.MensajeFcmMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-09: el adaptador FCM clasifica la respuesta del proveedor sin decidir la
 * política de reintentos, construye el payload mínimo acordado y no filtra el token en
 * logs, códigos ni resultados.
 */
class EntregaPushFirebaseAdapterTest {

    private static final String TOKEN = "token-fcm-de-prueba-no-debe-registrarse";
    private static final UUID NOTIFICACION = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOLICITUD = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String ID_PROVEEDOR = "projects/aliado-piloto/messages/123456";

    private EnvioFcmFalso envio;
    private EntregaPushFirebaseAdapter adaptador;

    @BeforeEach
    void preparar() {
        envio = new EnvioFcmFalso();
        adaptador = new EntregaPushFirebaseAdapter(envio,
                new MensajeFcmMapper(Duration.ofSeconds(3_600), "aliado_oportunidades"));
    }

    @Test
    void unaAceptacionRegistraElIdentificadorDelProveedorYElPayloadMinimo() {
        ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

        assertEquals(TipoResultadoEnvioPush.ACEPTADO, resultado.tipo());
        assertEquals(ID_PROVEEDOR, resultado.idMensaje(), "la aceptación conserva el ID de FCM");
        assertNull(resultado.codigo());
        assertNull(resultado.reintentarEn());

        MensajeFcm enviado = envio.enviados.get(0);
        assertEquals(TOKEN, enviado.token());
        assertEquals(MensajeFcmMapper.TITULO, enviado.titulo());
        assertEquals(MensajeFcmMapper.CUERPO, enviado.cuerpo());
        assertEquals(Map.of("tipo", "NUEVA_OPORTUNIDAD",
                        "notificacionId", NOTIFICACION.toString(),
                        "solicitudId", SOLICITUD.toString()),
                enviado.datos(),
                "el bloque de datos solo lleva los identificadores de navegación acordados");
        assertEquals(Duration.ofSeconds(3_600), enviado.ttl());
        assertEquals("aliado_oportunidades", enviado.canalAndroid());
        assertFalse(enviado.titulo().contains(TOKEN));
        assertFalse(enviado.cuerpo().contains(TOKEN));
        assertFalse(enviado.datos().toString().contains(TOKEN),
                "el token no viaja en el contenido visible ni en el bloque de datos");
    }

    @Test
    void cuotaYServicioNoDisponibleSeReintentanComoTemporales() {
        for (MessagingErrorCode codigo : List.of(MessagingErrorCode.QUOTA_EXCEEDED,
                MessagingErrorCode.UNAVAILABLE, MessagingErrorCode.INTERNAL)) {
            envio.fallo = new ExcepcionEnvioFcm(codigo, ErrorCode.UNAVAILABLE, null);

            ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

            assertEquals(TipoResultadoEnvioPush.TEMPORAL, resultado.tipo(), codigo.name());
            assertEquals("FCM_" + codigo.name(), resultado.codigo());
            assertNull(resultado.idMensaje());
        }
    }

    @Test
    void unTimeoutDelTransporteSeReintentaYConservaElRetryAfterCuandoExiste() {
        Instant reintentarEn = Instant.parse("2026-03-01T10:00:30Z");
        envio.fallo = new ExcepcionEnvioFcm(null, ErrorCode.DEADLINE_EXCEEDED, reintentarEn);

        ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

        assertEquals(TipoResultadoEnvioPush.TEMPORAL, resultado.tipo());
        assertEquals("FCM_DEADLINE_EXCEEDED", resultado.codigo());
        assertEquals(reintentarEn, resultado.reintentarEn(),
                "el adaptador traslada el Retry-After del proveedor a la aplicación");
    }

    @Test
    void unTokenDesregistradoRevocaSoloEseEnvio() {
        envio.fallo = new ExcepcionEnvioFcm(MessagingErrorCode.UNREGISTERED, null, null);

        ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

        assertEquals(TipoResultadoEnvioPush.TOKEN_DESREGISTRADO, resultado.tipo());
        assertEquals(CodigosEntregaPush.TOKEN_DESREGISTRADO, resultado.codigo());
    }

    @Test
    void unPayloadInvalidoOUnRemitenteIncorrectoNoSeTratanComoTokenDesregistrado() {
        for (MessagingErrorCode codigo : List.of(MessagingErrorCode.INVALID_ARGUMENT,
                MessagingErrorCode.SENDER_ID_MISMATCH,
                MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)) {
            envio.fallo = new ExcepcionEnvioFcm(codigo, ErrorCode.INVALID_ARGUMENT, null);

            ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

            assertEquals(TipoResultadoEnvioPush.PERMANENTE, resultado.tipo(), codigo.name());
            assertEquals("FCM_" + codigo.name(), resultado.codigo());
            assertFalse(resultado.codigo().contains(TOKEN));
        }
    }

    @Test
    void unasCredencialesIncorrectasSonUnFalloDeConfiguracionQueConservaElToken() {
        envio.fallo = new ExcepcionEnvioFcm(null, ErrorCode.UNAUTHENTICATED, null);

        ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

        assertEquals(TipoResultadoEnvioPush.PERMANENTE, resultado.tipo(),
                "un fallo de credenciales no debe revocar el vínculo del dispositivo");
        assertEquals(CodigosEntregaPush.PROVEEDOR_NO_CONFIGURADO, resultado.codigo());
    }

    @Test
    void unTipoNoHabilitadoNoContactaAlProveedor() {
        ResultadoEnvioPush resultado = adaptador.enviar(mensaje(TipoNotificacion.ACEPTACION_PROPUESTA));

        assertEquals(TipoResultadoEnvioPush.PERMANENTE, resultado.tipo());
        assertEquals(CodigosEntregaPush.TIPO_NO_SOPORTADO, resultado.codigo());
        assertTrue(envio.enviados.isEmpty(), "el piloto solo envía NUEVA_OPORTUNIDAD por FCM");
    }

    @Test
    void niLosLogsNiLosResultadosIncluyenElToken() {
        Logger logger = (Logger) LoggerFactory.getLogger(EntregaPushFirebaseAdapter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            envio.fallo = new ExcepcionEnvioFcm(null, ErrorCode.UNAUTHENTICATED, null);
            adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));
            envio.fallo = new ExcepcionEnvioFcm(MessagingErrorCode.UNREGISTERED, null, null);
            adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));
            envio.fallo = null;
            adaptador.enviar(mensaje(TipoNotificacion.NUEVA_OPORTUNIDAD));

            assertTrue(appender.list.stream()
                            .anyMatch(evento -> evento.getFormattedMessage().contains("FCM aceptó")),
                    "la aceptación queda registrada");
            assertTrue(appender.list.stream()
                            .anyMatch(evento -> evento.getFormattedMessage().contains("FCM no aceptó")),
                    "el rechazo queda registrado con su código");
            for (ILoggingEvent evento : appender.list) {
                assertFalse(evento.getFormattedMessage().contains(TOKEN),
                        "ningún log puede reproducir el token");
                if (evento.getArgumentArray() != null) {
                    for (Object argumento : evento.getArgumentArray()) {
                        if (argumento != null) {
                            assertFalse(argumento.toString().contains(TOKEN));
                        }
                    }
                }
            }
        } finally {
            logger.detachAppender(appender);
        }
    }

    private static MensajePush mensaje(TipoNotificacion tipo) {
        return new MensajePush(NOTIFICACION, SOLICITUD, tipo, TOKEN);
    }

    /** Doble del transporte: devuelve una aceptación o el fallo configurado. */
    private static final class EnvioFcmFalso implements EnvioFcm {
        private final List<MensajeFcm> enviados = new ArrayList<>();
        private ExcepcionEnvioFcm fallo;

        @Override
        public String enviar(MensajeFcm mensaje) throws ExcepcionEnvioFcm {
            enviados.add(mensaje);
            if (fallo != null) {
                throw fallo;
            }
            return ID_PROVEEDOR;
        }
    }
}
