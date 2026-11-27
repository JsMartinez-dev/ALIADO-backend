package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.NotificacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaRegistrado;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarAceptacionPropuestaService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.NotificarCambioEstadoServicioService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Test void cadaAvisoRegistradoDisparaSoloSuPropiaEntrega() {
        NotificacionRepository repository = mock(NotificacionRepository.class);
        ConsultaPreferenciasPort preferencias = mock(ConsultaPreferenciasPort.class);
        PublicadorEventos eventos = mock(PublicadorEventos.class);
        when(repository.existeEvento(any(), any(), any(), anyInt())).thenReturn(false);
        when(preferencias.avisoHabilitado(any(), any(), any())).thenReturn(true);
        var servicio = new NotificacionService(repository, preferencias, eventos);
        servicio.registrar(TipoNotificacion.ACEPTACION_PROPUESTA, UUID.randomUUID(), UUID.randomUUID(), 1,
                "aceptada", Canal.PUSH);
        servicio.registrar(TipoNotificacion.CAMBIO_ESTADO_SERVICIO, UUID.randomUUID(), UUID.randomUUID(), 2,
                "cambio", Canal.PUSH);
        var guardadas = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, times(2)).guardar(guardadas.capture());
        var avisos = ArgumentCaptor.forClass(AvisoBandejaRegistrado.class);
        verify(eventos, times(2)).publicar(avisos.capture());
        // Un evento por aviso registrado, y el identificador es el del aviso guardado:
        // no hay barrido de la tabla ni entrega de avisos ajenos.
        assertEquals(List.of(guardadas.getAllValues().get(0).id(), guardadas.getAllValues().get(1).id()),
                avisos.getAllValues().stream().map(AvisoBandejaRegistrado::notificacionId).toList());
    }

    @Test void unEventoYaProcesadoNoVuelveADispararEntrega() {
        NotificacionRepository repository = mock(NotificacionRepository.class);
        ConsultaPreferenciasPort preferencias = mock(ConsultaPreferenciasPort.class);
        PublicadorEventos eventos = mock(PublicadorEventos.class);
        when(repository.existeEvento(any(), any(), any(), anyInt())).thenReturn(true);
        new NotificacionService(repository, preferencias, eventos).registrar(
                TipoNotificacion.NUEVA_OPORTUNIDAD, UUID.randomUUID(), UUID.randomUUID(), 1,
                "oportunidad", Canal.PUSH);
        verify(repository, never()).guardar(any());
        verifyNoInteractions(eventos, preferencias);
    }
}
