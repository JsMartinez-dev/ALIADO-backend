package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.NotificacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarAceptacionPropuestaService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarCambioEstadoServicioService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.LogCanalEntregaAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

class NotificacionEventosUseCasesTest {
    @Test void aceptacionNotificaAlPrestador() {
        NotificacionPort registro = mock(NotificacionPort.class);
        UUID servicio = UUID.randomUUID(), prestador = UUID.randomUUID();
        new NotificarAceptacionPropuestaService(registro).ejecutar(new Eventos.PropuestaAceptada(
                servicio, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), prestador, OffsetDateTime.now()));
        verify(registro).registrar(eq(TipoNotificacion.ACEPTACION_PROPUESTA), eq(servicio), eq(prestador), eq(1), anyString(), eq(Canal.PUSH));
    }

    @Test void cambioDelPrestadorNotificaAlCliente() {
        NotificacionPort registro = mock(NotificacionPort.class);
        UUID servicio = UUID.randomUUID(), cliente = UUID.randomUUID(), prestador = UUID.randomUUID();
        new NotificarCambioEstadoServicioService(registro).ejecutar(new Eventos.ServicioEstadoCambiado(
                servicio, cliente, prestador, "CONTRATADO", "EN_EJECUCION", prestador, OffsetDateTime.now(), 2));
        verify(registro).registrar(eq(TipoNotificacion.CAMBIO_ESTADO_SERVICIO), eq(servicio), eq(cliente), eq(2), contains("EN_EJECUCION"), eq(Canal.PUSH));
    }

    @Test void cambioDelClienteNotificaAlPrestador() {
        NotificacionPort registro = mock(NotificacionPort.class);
        UUID servicio = UUID.randomUUID(), cliente = UUID.randomUUID(), prestador = UUID.randomUUID();
        new NotificarCambioEstadoServicioService(registro).ejecutar(new Eventos.ServicioEstadoCambiado(
                servicio, cliente, prestador, "TERMINADO", "FINALIZADO", cliente, OffsetDateTime.now(), 4));
        verify(registro).registrar(eq(TipoNotificacion.CAMBIO_ESTADO_SERVICIO), eq(servicio), eq(prestador), eq(4), contains("FINALIZADO"), eq(Canal.PUSH));
    }

    @Test void logNoConsumeIntentosDeAceptacionNiCambio() {
        NotificacionRepository repository = mock(NotificacionRepository.class);
        ConsultaPreferenciasPort preferencias = mock(ConsultaPreferenciasPort.class);
        var ahora = OffsetDateTime.now();
        var aceptacion = new Notificacion(UUID.randomUUID(), UUID.randomUUID(), TipoNotificacion.ACEPTACION_PROPUESTA,
                UUID.randomUUID(), 1, "aceptada", Canal.PUSH, EstadoEntrega.PENDIENTE, 0, ahora, null);
        var cambio = new Notificacion(UUID.randomUUID(), UUID.randomUUID(), TipoNotificacion.CAMBIO_ESTADO_SERVICIO,
                UUID.randomUUID(), 2, "cambio", Canal.PUSH, EstadoEntrega.PENDIENTE, 0, ahora, null);
        when(repository.pendientesParaEntrega()).thenReturn(List.of(aceptacion, cambio));
        new NotificacionService(repository, new LogCanalEntregaAdapter(), preferencias).entregarPendientes();
        verify(repository, never()).guardar(any());
        verifyNoInteractions(preferencias);
    }
}
