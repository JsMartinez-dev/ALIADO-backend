package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResolverOportunidadAvisoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.EstadoOportunidadAvisoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ResolverOportunidadAvisoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoOportunidadAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper.NotificacionPresentationMapper;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * F09-PUSH-10: el destinatario distingue oportunidad abierta y cerrada; el resto de casos
 * usa el mismo error genérico sin consultar la oportunidad.
 */
class ResolverOportunidadAvisoUseCaseTest {

    private static final UUID PRESTADOR = UUID.randomUUID();
    private static final UUID OTRA_CUENTA = UUID.randomUUID();
    private static final UUID SOLICITUD = UUID.randomUUID();
    private static final UUID AVISO = UUID.randomUUID();

    private NotificacionRepository notificaciones;
    private EstadoOportunidadAvisoPort estado;
    private ResolverOportunidadAvisoService service;

    @BeforeEach
    void preparar() {
        notificaciones = mock(NotificacionRepository.class);
        estado = mock(EstadoOportunidadAvisoPort.class);
        service = new ResolverOportunidadAvisoService(notificaciones, estado);
    }

    @Test
    void avisoAbiertoDevuelveAbiertaYPuedeCotizar() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.of(
                aviso(PRESTADOR, TipoNotificacion.NUEVA_OPORTUNIDAD)));
        when(estado.estado(SOLICITUD, PRESTADOR)).thenReturn(Optional.of(EstadoOportunidadAviso.ABIERTA));

        var result = service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR));

        assertEquals(SOLICITUD, result.solicitudId());
        assertEquals(EstadoOportunidadAviso.ABIERTA, result.estado());
        assertTrue(result.puedeCotizar());
        var response = NotificacionPresentationMapper.aResponse(result);
        assertEquals(SOLICITUD, response.solicitudId());
        assertEquals("ABIERTA", response.estado());
        assertTrue(response.puedeCotizar());
    }

    @Test
    void avisoCerradoDevuelveSoloElEstadoMinimoYPuedeCotizarFalso() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.of(
                aviso(PRESTADOR, TipoNotificacion.NUEVA_OPORTUNIDAD)));
        when(estado.estado(SOLICITUD, PRESTADOR)).thenReturn(Optional.of(EstadoOportunidadAviso.CERRADA));

        var result = service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR));

        assertEquals(SOLICITUD, result.solicitudId());
        assertEquals(EstadoOportunidadAviso.CERRADA, result.estado());
        assertFalse(result.puedeCotizar());
        var response = NotificacionPresentationMapper.aResponse(result);
        assertEquals(SOLICITUD, response.solicitudId());
        assertEquals("CERRADA", response.estado());
        assertFalse(response.puedeCotizar());
    }

    @Test
    void avisoAjenoNoConsultaLaOportunidadYUsaElErrorGenerico() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.of(
                aviso(OTRA_CUENTA, TipoNotificacion.NUEVA_OPORTUNIDAD)));

        var ex = assertThrows(BusinessException.class,
                () -> service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR)));

        assertEquals("OPORTUNIDAD_NO_DISPONIBLE", ex.code());
        assertEquals(404, ex.status());
        verifyNoInteractions(estado);
    }

    @Test
    void avisoInexistenteUsaElErrorGenerico() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.empty());

        var ex = assertThrows(BusinessException.class,
                () -> service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR)));

        assertEquals("OPORTUNIDAD_NO_DISPONIBLE", ex.code());
        assertEquals(404, ex.status());
        verifyNoInteractions(estado);
    }

    @Test
    void avisoDeOtroTipoUsaElErrorGenerico() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.of(
                aviso(PRESTADOR, TipoNotificacion.ACEPTACION_PROPUESTA)));

        var ex = assertThrows(BusinessException.class,
                () -> service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR)));

        assertEquals("OPORTUNIDAD_NO_DISPONIBLE", ex.code());
        assertEquals(404, ex.status());
        verifyNoInteractions(estado);
    }

    @Test
    void oportunidadInexistenteOsinAccesoUsaElMismoErrorGenerico() {
        when(notificaciones.porId(AVISO)).thenReturn(Optional.of(
                aviso(PRESTADOR, TipoNotificacion.NUEVA_OPORTUNIDAD)));
        when(estado.estado(SOLICITUD, PRESTADOR)).thenReturn(Optional.empty());

        var ex = assertThrows(BusinessException.class,
                () -> service.ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR)));

        assertEquals("OPORTUNIDAD_NO_DISPONIBLE", ex.code());
        assertEquals(404, ex.status());
        assertEquals("Oportunidad no disponible.", ex.getMessage());
    }

    @Test
    void todosLosRechazosCompartenCodigoEstadoYMensaje() {
        BusinessException ajeno = rechazo(aviso(OTRA_CUENTA, TipoNotificacion.NUEVA_OPORTUNIDAD), null);
        BusinessException otroTipo = rechazo(aviso(PRESTADOR, TipoNotificacion.CAMBIO_ESTADO_SERVICIO), null);
        BusinessException sinOportunidad = rechazo(
                aviso(PRESTADOR, TipoNotificacion.NUEVA_OPORTUNIDAD), Optional.empty());

        assertEquals(ajeno.code(), otroTipo.code());
        assertEquals(ajeno.code(), sinOportunidad.code());
        assertEquals(ajeno.status(), otroTipo.status());
        assertEquals(ajeno.status(), sinOportunidad.status());
        assertEquals(ajeno.getMessage(), otroTipo.getMessage());
        assertEquals(ajeno.getMessage(), sinOportunidad.getMessage());
    }

    private BusinessException rechazo(Notificacion aviso, Optional<EstadoOportunidadAviso> estadoOportunidad) {
        NotificacionRepository repo = mock(NotificacionRepository.class);
        EstadoOportunidadAvisoPort puerto = mock(EstadoOportunidadAvisoPort.class);
        when(repo.porId(AVISO)).thenReturn(Optional.of(aviso));
        if (estadoOportunidad != null) {
            when(puerto.estado(SOLICITUD, PRESTADOR)).thenReturn(estadoOportunidad);
        }
        return assertThrows(BusinessException.class,
                () -> new ResolverOportunidadAvisoService(repo, puerto)
                        .ejecutar(new ResolverOportunidadAvisoCommand(AVISO, PRESTADOR)));
    }

    private Notificacion aviso(UUID destinatario, TipoNotificacion tipo) {
        return new Notificacion(AVISO, destinatario, tipo, SOLICITUD, 1, "detalle",
                Canal.PUSH, EstadoEntrega.ENTREGADA, 1, OffsetDateTime.now(), null);
    }
}
