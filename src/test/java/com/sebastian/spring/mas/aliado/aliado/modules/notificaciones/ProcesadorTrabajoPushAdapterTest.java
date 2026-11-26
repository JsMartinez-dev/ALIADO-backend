package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.google.firebase.messaging.MessagingErrorCode;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EntregaPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarResultadoEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.IniciarEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.PrepararEntregasPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarResultadoEntregaPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ProcesadorTrabajoPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.EntregaPushFirebaseAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.ExcepcionEnvioFcm;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.fcm.MensajeFcmMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F09-PUSH-09: el adaptador FCM se invoca fuera de la transacción de reclamación y de la
 * de resultado, y la caída posterior a una aceptación deja documentada la ventana en la
 * que el push puede repetirse (D-08: no se promete una única entrega).
 */
class ProcesadorTrabajoPushAdapterTest {

    private static final UUID TRABAJO = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ENTREGA = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID INSTALACION = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID RECLAMACION = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID NOTIFICACION = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID SOLICITUD = UUID.fromString("88888888-8888-8888-8888-888888888888");

    private TransactionTemplate transaccion;

    @BeforeEach
    void preparar() {
        transaccion = new TransactionTemplate(new GestorTransaccional());
    }

    @Test
    void elProveedorSeInvocaFueraDeLaTransaccionYElResultadoSeGuardaDentroDeOtra() {
        EnvioPushPendiente envio = envio();
        List<String> fases = new ArrayList<>();
        AtomicReference<String> idGuardado = new AtomicReference<>();

        ProcesadorTrabajoPushAdapter procesador = procesador(
                comando -> {
                    fases.add("inicio:" + transaccionActiva());
                    return Optional.of(envio);
                },
                mensaje -> {
                    fases.add("envio:" + transaccionActiva());
                    return ResultadoEnvioPush.aceptado("fcm-1");
                },
                comando -> {
                    fases.add("resultado:" + transaccionActiva());
                    idGuardado.set(comando.resultado().idMensaje());
                });

        procesador.procesar(reclamacion());

        assertEquals(List.of("inicio:true", "envio:false", "resultado:true"), fases,
                "la llamada externa queda entre dos transacciones cortas, nunca dentro de una");
        assertEquals("fcm-1", idGuardado.get(), "la aceptación de FCM se registra con su ID");
        assertEquals(false, transaccionActiva(), "el procesador no deja una transacción abierta");
    }

    @Test
    void unaCaidaDespuesDeLaAceptacionPuedeRepetirElPushAlRecuperarLaReclamacion() {
        EnvioPushPendiente envio = envio();
        List<String> idsAceptados = new ArrayList<>();
        AtomicInteger envios = new AtomicInteger();
        AtomicInteger resultados = new AtomicInteger();
        AtomicReference<String> idGuardado = new AtomicReference<>();

        EntregaPushPort entrega = mensaje -> {
            String id = "fcm-" + envios.incrementAndGet();
            idsAceptados.add(id);
            return ResultadoEnvioPush.aceptado(id);
        };
        RegistrarResultadoEntregaPushUseCase registrar = comando -> {
            if (resultados.incrementAndGet() == 1) {
                // Equivale a la caída del worker después de que FCM aceptó y antes del
                // commit del resultado: la fila conserva la reclamación persistida.
                throw new IllegalStateException("caída simulada antes de guardar la aceptación");
            }
            idGuardado.set(comando.resultado().idMensaje());
        };
        ProcesadorTrabajoPushAdapter procesador = procesador(
                comando -> Optional.of(envio), entrega, registrar);

        assertThrows(IllegalStateException.class, () -> procesador.procesar(reclamacion()));
        assertEquals(1, envios.get(), "FCM aceptó el primer envío aunque el resultado no se guardó");
        assertNull(idGuardado.get());

        // Otra instancia recupera la reclamación vencida y vuelve a enviar el mismo aviso:
        // la ventana de duplicación existe y no se puede prometer exactamente una entrega.
        procesador.procesar(reclamacion());
        assertEquals(2, envios.get());
        assertEquals(List.of("fcm-1", "fcm-2"), idsAceptados);
        assertEquals("fcm-2", idGuardado.get());
    }

    @Test
    void unTokenDesregistradoDelProveedorRevocaSoloLaGeneracionEnviada() {
        ReclamacionTrabajoPushRepository reclamaciones = mock(ReclamacionTrabajoPushRepository.class);
        DispositivoPushRepository dispositivos = mock(DispositivoPushRepository.class);
        when(reclamaciones.guardarResultadoEntrega(any())).thenReturn(true);
        RegistrarResultadoEntregaPushService registrar = new RegistrarResultadoEntregaPushService(
                reclamaciones, dispositivos,
                new PoliticaReintentoPush(5, Duration.ofSeconds(30), () -> 1.0),
                Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC));

        // Cadena real: adaptador FCM -> procesador -> caso de uso de resultado.
        EntregaPushPort adaptador = new EntregaPushFirebaseAdapter(mensaje -> {
            throw new ExcepcionEnvioFcm(MessagingErrorCode.UNREGISTERED, null, null);
        }, new MensajeFcmMapper(Duration.ofHours(1), "aliado_oportunidades"));
        ProcesadorTrabajoPushAdapter procesador = procesador(
                comando -> Optional.of(envio()), adaptador, registrar);

        procesador.procesar(reclamacion());

        ArgumentCaptor<ResultadoEntregaPush> captor =
                ArgumentCaptor.forClass(ResultadoEntregaPush.class);
        verify(reclamaciones).guardarResultadoEntrega(captor.capture());
        assertEquals(EstadoEntregaPush.SUPRIMIDA, captor.getValue().estado());
        assertEquals(CodigosEntregaPush.TOKEN_DESREGISTRADO,
                captor.getValue().ultimoCodigoError());
        verify(dispositivos).revocarVinculo(eq(INSTALACION), eq(1L), any(OffsetDateTime.class));
        verify(dispositivos, never()).revocarVinculo(any(), eq(2L), any(OffsetDateTime.class));
        verify(reclamaciones).cerrarPadreSiCorresponde(TRABAJO);
    }

    private ProcesadorTrabajoPushAdapter procesador(IniciarEntregaPushUseCase iniciar,
                                                     EntregaPushPort entrega,
                                                     RegistrarResultadoEntregaPushUseCase registrar) {
        PrepararEntregasPushUseCase preparar = comando -> {
            throw new AssertionError("esta prueba solo procesa reclamaciones de envío");
        };
        return new ProcesadorTrabajoPushAdapter(preparar, iniciar, registrar, entrega, transaccion);
    }

    private static boolean transaccionActiva() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    private static ReclamacionPush reclamacion() {
        return ReclamacionPush.envio(ENTREGA, TRABAJO, INSTALACION, 1, RECLAMACION,
                Instant.now().plusSeconds(30));
    }

    private static EnvioPushPendiente envio() {
        return new EnvioPushPendiente(ENTREGA, TRABAJO, INSTALACION, 1, RECLAMACION, 0,
                Instant.now().plusSeconds(3_600),
                new MensajePush(NOTIFICACION, SOLICITUD, TipoNotificacion.NUEVA_OPORTUNIDAD,
                        "token-fcm-de-prueba"));
    }

    /**
     * Gestor mínimo que marca la transacción como activa, para comprobar el límite real
     * del procesador sin abrir una conexión a base de datos.
     */
    private static final class GestorTransaccional implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definicion) {
            TransactionSynchronizationManager.setActualTransactionActive(true);
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.initSynchronization();
            }
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus estado) {
            cerrar();
        }

        @Override
        public void rollback(TransactionStatus estado) {
            cerrar();
        }

        private static void cerrar() {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }
    }
}
