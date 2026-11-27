package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.cursor.CursorBandeja;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.BandejaAvisosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarBandejaService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ConsultarResumenNoLeidasService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * F09-BANDEJA-03 en el caso de uso: valores por defecto, combinación de filtros, límite + 1,
 * cursor de la última fila devuelta y rechazo controlado de cursores inválidos o de otros
 * filtros. El destinatario usado por el repositorio es siempre el de la sesión.
 */
class ConsultarBandejaUseCasesTest {

    private static final UUID CUENTA = UUID.randomUUID();
    private static final UUID OTRA_CUENTA = UUID.randomUUID();
    private static final OffsetDateTime GENERADA =
            OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final UUID ID_MAYOR = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ID_MENOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final NotificacionRepository notificaciones = mock(NotificacionRepository.class);
    private final ConsultarBandejaService bandeja = new ConsultarBandejaService(notificaciones, (propuesta, destinatario) -> java.util.Optional.empty());
    private final ConsultarResumenNoLeidasService resumen =
            new ConsultarResumenNoLeidasService(notificaciones);

    @Test
    void elCasoPorDefectoConsultaPendientesPropiasConVeinteElementos() {
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 21))
                .thenReturn(List.of(aviso(ID_MAYOR, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null)));

        BandejaAvisosResult resultado = bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, null, null, null, null));

        assertEquals(1, resultado.items().size());
        assertEquals(ID_MAYOR, resultado.items().get(0).id());
        assertEquals(TipoNotificacion.NUEVA_OPORTUNIDAD.name(), resultado.items().get(0).tipo());
        assertNotNull(resultado.items().get(0).solicitudId(), "el agregado de oportunidad es la solicitud");
        assertNull(resultado.items().get(0).servicioId());
        assertNull(resultado.items().get(0).leidaEn());
        assertNull(resultado.cursorSiguiente());
        verify(notificaciones).buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 21);
    }

    @Test
    void unAvisoDePropuestaSoloEnlazaUnaSolicitudPropia() {
        UUID solicitudId = UUID.randomUUID();
        var avisoPropio = aviso(ID_MAYOR, TipoNotificacion.PROPUESTA_RECIBIDA, GENERADA, null);
        var avisoSinReferencia = aviso(ID_MENOR, TipoNotificacion.PROPUESTA_RECIBIDA,
                GENERADA.minusMinutes(1), null);
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 21))
                .thenReturn(List.of(avisoPropio, avisoSinReferencia));
        var consultar = new ConsultarBandejaService(notificaciones, (propuestaId, destinatarioId) ->
                avisoPropio.aggregateId().equals(propuestaId) && CUENTA.equals(destinatarioId)
                        ? Optional.of(solicitudId) : Optional.empty());

        var items = consultar.ejecutar(new ConsultarBandejaCommand(CUENTA, null, null, null, null)).items();

        assertEquals(solicitudId, items.get(0).solicitudId());
        assertEquals(avisoPropio.aggregateId(), items.get(0).propuestaId());
        assertNull(items.get(1).solicitudId());
        assertNull(items.get(1).propuestaId());
    }

    @Test
    void elEstadoYElTipoSeCombinanEnLaMismaConsulta() {
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.LEIDA,
                TipoNotificacion.PROPUESTA_RECIBIDA, null, null, 6)).thenReturn(List.of());

        BandejaAvisosResult resultado = bandeja.ejecutar(new ConsultarBandejaCommand(
                CUENTA, "LEIDA", "PROPUESTA_RECIBIDA", 5, null));

        assertTrue(resultado.items().isEmpty());
        assertNull(resultado.cursorSiguiente());
        verify(notificaciones).buscarBandeja(CUENTA, EstadoLectura.LEIDA,
                TipoNotificacion.PROPUESTA_RECIBIDA, null, null, 6);
    }

    @Test
    void unaBandejaVaciaDevuelveListaVaciaYCursorNulo() {
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 21))
                .thenReturn(List.of());

        BandejaAvisosResult resultado = bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, null, null, null, null));

        assertTrue(resultado.items().isEmpty());
        assertNull(resultado.cursorSiguiente());
    }

    @Test
    void laUltimaPaginaNoDevuelveCursor() {
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 3))
                .thenReturn(List.of(
                        aviso(ID_MAYOR, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null),
                        aviso(ID_MENOR, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA.minusMinutes(1), null)));

        BandejaAvisosResult resultado = bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, null, null, 2, null));

        assertEquals(2, resultado.items().size());
        assertNull(resultado.cursorSiguiente());
    }

    @Test
    void elCursorSeConstruyeConElParFechaIdDeLaUltimaFilaDevueltaAunqueLasFechasCoincidan() {
        Notificacion primera = aviso(ID_MAYOR, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null);
        Notificacion segunda = aviso(ID_MENOR, TipoNotificacion.NUEVA_OPORTUNIDAD, GENERADA, null);
        Notificacion adicional = aviso(UUID.randomUUID(), TipoNotificacion.NUEVA_OPORTUNIDAD,
                GENERADA.minusMinutes(1), null);
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null, null, null, 3))
                .thenReturn(List.of(primera, segunda, adicional));

        BandejaAvisosResult resultado = bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, null, null, 2, null));

        assertEquals(List.of(ID_MAYOR, ID_MENOR),
                resultado.items().stream().map(a -> a.id()).toList());
        assertNotNull(resultado.cursorSiguiente());

        CursorBandeja cursor = CursorBandeja.decodificar(resultado.cursorSiguiente(),
                EstadoLectura.NO_LEIDA, null);
        assertEquals(GENERADA.toInstant(), cursor.fechaGeneracion().toInstant());
        assertEquals(ID_MENOR, cursor.id(), "el desempate usa el ID de la última fila devuelta");
    }

    @Test
    void elCursorSeAplicaComoCorteDelParFechaIdSinCambiarElDestinatario() {
        String cursor = CursorBandeja.codificar(GENERADA, ID_MENOR, EstadoLectura.NO_LEIDA, null);
        when(notificaciones.buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null,
                GENERADA, ID_MENOR, 21)).thenReturn(List.of());

        BandejaAvisosResult resultado = bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, null, null, null, cursor));

        assertTrue(resultado.items().isEmpty());
        assertNull(resultado.cursorSiguiente());
        verify(notificaciones).buscarBandeja(CUENTA, EstadoLectura.NO_LEIDA, null,
                GENERADA, ID_MENOR, 21);
    }

    @Test
    void unCursorReutilizadoPorOtraCuentaConsultaSiempreLaCuentaDeLaSesion() {
        // El cursor no contiene cuenta: aunque se reutilice, el repositorio recibe la sesión.
        String cursorDeCuenta = CursorBandeja.codificar(GENERADA, ID_MENOR,
                EstadoLectura.NO_LEIDA, null);
        when(notificaciones.buscarBandeja(OTRA_CUENTA, EstadoLectura.NO_LEIDA, null,
                GENERADA, ID_MENOR, 21)).thenReturn(List.of());

        bandeja.ejecutar(new ConsultarBandejaCommand(OTRA_CUENTA, null, null, null, cursorDeCuenta));

        verify(notificaciones).buscarBandeja(OTRA_CUENTA, EstadoLectura.NO_LEIDA, null,
                GENERADA, ID_MENOR, 21);
        verify(notificaciones, never()).buscarBandeja(org.mockito.ArgumentMatchers.eq(CUENTA),
                any(), any(), any(), any(), anyInt());
    }

    @Test
    void unCursorDeOtrosFiltrosSeRechaza() {
        String cursorPendientes = CursorBandeja.codificar(GENERADA, ID_MENOR,
                EstadoLectura.NO_LEIDA, null);

        BusinessException error = assertThrows(BusinessException.class, () -> bandeja.ejecutar(
                new ConsultarBandejaCommand(CUENTA, "TODAS", null, null, cursorPendientes)));

        assertEquals("CURSOR_INVALIDO", error.code());
        assertEquals(400, error.status());
        verifyNoInteractions(notificaciones);
    }

    @Test
    void unCursorMalFormadoSeRechazaSinConsultarElRepositorio() {
        String versionDesconocida = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("v2|1|0|00000000-0000-0000-0000-000000000001|NO_LEIDA:"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        for (String cursor : List.of("no-es-un-cursor", versionDesconocida, "%%%%")) {
            BusinessException error = assertThrows(BusinessException.class, () -> bandeja.ejecutar(
                    new ConsultarBandejaCommand(CUENTA, null, null, null, cursor)));
            assertEquals("CURSOR_INVALIDO", error.code(), cursor);
            assertEquals(400, error.status());
        }
        verifyNoInteractions(notificaciones);
    }

    @Test
    void estadoTipoYLimiteInvalidosSeRechazanDeFormaControlada() {
        assertEquals("ESTADO_LECTURA_INVALIDO", assertThrows(BusinessException.class,
                () -> bandeja.ejecutar(new ConsultarBandejaCommand(CUENTA, "PENDIENTE", null, null, null)))
                .code());
        assertEquals("TIPO_INVALIDO", assertThrows(BusinessException.class,
                () -> bandeja.ejecutar(new ConsultarBandejaCommand(CUENTA, null, "OTRO", null, null)))
                .code());
        assertEquals("LIMITE_INVALIDO", assertThrows(BusinessException.class,
                () -> bandeja.ejecutar(new ConsultarBandejaCommand(CUENTA, null, null, 0, null)))
                .code());
        assertEquals("LIMITE_INVALIDO", assertThrows(BusinessException.class,
                () -> bandeja.ejecutar(new ConsultarBandejaCommand(CUENTA, null, null, 101, null)))
                .code());
        verifyNoInteractions(notificaciones);
    }

    @Test
    void elResumenSoloConsultaLaExistenciaDePendientesDeLaCuenta() {
        when(notificaciones.hayNoLeidas(CUENTA)).thenReturn(true);
        when(notificaciones.hayNoLeidas(OTRA_CUENTA)).thenReturn(false);

        assertTrue(resumen.ejecutar(CUENTA).hayNoLeidas());
        assertFalse(resumen.ejecutar(OTRA_CUENTA).hayNoLeidas());
        assertEquals("DATOS_INVALIDOS", assertThrows(BusinessException.class,
                () -> resumen.ejecutar(null)).code());
    }

    private static Notificacion aviso(UUID id, TipoNotificacion tipo, OffsetDateTime fecha,
                                      OffsetDateTime leidaEn) {
        return new Notificacion(id, CUENTA, tipo, UUID.randomUUID(), 1, "detalle " + id,
                Canal.PUSH, EstadoEntrega.ENTREGADA, 1, fecha, null, leidaEn);
    }
}
