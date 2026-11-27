package com.sebastian.spring.mas.aliado.aliado.modules.preferencias;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActualizarPreferenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargarHistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.HistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception.PreferenciasOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ArchivoHistorialPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ActividadNotificacionesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.IndicadoresPrestadorPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ActualizarPreferenciaService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarActividadRecienteService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ConsultarIndicadoresPrestadorService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.DescargarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ExportarHistorialService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service.ListarPreferenciasService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.DescargaHistorial;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.PreferenciaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.TipoAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.DescargaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PreferenciasUseCasesTest {
    private final UUID usuario = UUID.randomUUID();

    @Test
    void rechazaDeshabilitarAvisoObligatorioAntesDePersistir() {
        var repo = mock(PreferenciaRepository.class);
        var command = new ActualizarPreferenciaCommand(usuario, "AVISO_SEGURIDAD_CUENTA", "PUSH", false);

        var error = assertThrows(PreferenciasOperacionException.class,
                () -> new ActualizarPreferenciaService(repo).ejecutar(command));
        assertEquals("AVISO_NO_CONFIGURABLE", error.codigo());
        verifyNoInteractions(repo);
    }

    @Test
    void listaAvisosObligatoriosHabilitadosInclusoConDatosAnteriores() {
        var repo = mock(PreferenciaRepository.class);
        when(repo.porUsuario(usuario)).thenReturn(List.of(
                new PreferenciaNotificacion(usuario, TipoAviso.AVISO_SEGURIDAD_CUENTA, "PUSH", false, true)));

        var obligatorios = new ListarPreferenciasService(repo).ejecutar(usuario).stream()
                .filter(p -> p.tipoAviso().equals("AVISO_SEGURIDAD_CUENTA")).toList();

        assertEquals(2, obligatorios.size());
        assertTrue(obligatorios.stream().allMatch(p -> p.habilitada() && !p.configurable()));
    }

    @Test
    void historialRechazaPaginasInvalidasAntesDeConsultar() {
        var servicios = mock(HistorialServiciosPort.class);
        var caso = new ConsultarHistorialService(servicios);

        assertThrows(PreferenciasOperacionException.class, () -> caso.ejecutar(new HistorialQuery(usuario, 0, 101)));
        verifyNoInteractions(servicios);
    }

    @Test
    void exportaTodasLasPaginasConUnSoloEncabezado() {
        var servicios = mock(HistorialServiciosPort.class);
        var descargas = mock(DescargaRepository.class);
        var archivos = mock(ArchivoHistorialPort.class);
        var primera = IntStream.range(0, 100).mapToObj(i -> servicio(EstadoServicio.CONTRATADO)).toList();
        var ultimo = servicio(EstadoServicio.CANCELADO);
        when(servicios.porUsuario(usuario, 0, 100)).thenReturn(primera);
        when(servicios.porUsuario(usuario, 1, 100)).thenReturn(List.of(ultimo));
        when(archivos.guardar(any())).thenReturn("clave");
        when(descargas.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = new ExportarHistorialService(servicios, descargas, archivos).ejecutar(usuario);

        assertEquals("LISTA", resultado.estado());
        var contenido = ArgumentCaptor.forClass(byte[].class);
        verify(archivos).guardar(contenido.capture());
        var filas = new String(contenido.getValue(), StandardCharsets.UTF_8).lines().toList();
        assertEquals(102, filas.size());
        assertEquals("servicio_id,estado,fecha_contratacion", filas.getFirst());
        assertTrue(filas.getLast().startsWith(ultimo.id() + ",CANCELADO,"));
        verify(servicios, never()).porUsuario(usuario);
    }

    @Test
    void exportacionAjenaNoLeeArchivo() {
        var descargas = mock(DescargaRepository.class);
        var archivos = mock(ArchivoHistorialPort.class);
        var id = UUID.randomUUID();
        when(descargas.porId(id)).thenReturn(Optional.of(
                new DescargaHistorial(id, UUID.randomUUID(), OffsetDateTime.now(), "clave", "LISTA")));

        var error = assertThrows(PreferenciasOperacionException.class,
                () -> new DescargarHistorialService(descargas, archivos)
                        .ejecutar(new DescargarHistorialQuery(usuario, id)));
        assertEquals("DESCARGA_NO_ENCONTRADA", error.codigo());
        verifyNoInteractions(archivos);
    }

    @Test
    void indicadoresCuentanTerminadoYFinalizadoYConservanCanceladosEnTotal() {
        var servicios = mock(HistorialServiciosPort.class);
        var datos = mock(IndicadoresPrestadorPort.class);
        when(datos.esPrestador(usuario)).thenReturn(true);
        when(datos.reputacionDe(usuario)).thenReturn(new IndicadoresPrestadorPort.Reputacion(4.5, 2));
        when(servicios.porUsuario(usuario)).thenReturn(List.of(servicio(EstadoServicio.TERMINADO),
                servicio(EstadoServicio.FINALIZADO), servicio(EstadoServicio.CANCELADO),
                servicio(EstadoServicio.EN_EJECUCION)));

        var resultado = new ConsultarIndicadoresPrestadorService(servicios, datos).ejecutar(usuario);

        assertEquals(4, resultado.totalServicios());
        assertEquals(2, resultado.finalizados());
        assertEquals(1, resultado.enCurso());
    }

    @Test
    void actividadDesempataFechasDeFormaEstable() {
        var servicios = mock(HistorialServiciosPort.class);
        var notificaciones = mock(ActividadNotificacionesPort.class);
        var mismaFecha = OffsetDateTime.parse("2026-11-20T10:00:00-05:00");
        when(notificaciones.listarPropias(usuario)).thenReturn(List.of(
                new ActividadNotificacionesPort.ActividadNotificacion(
                        UUID.fromString("00000000-0000-0000-0000-0000000000aa"), "AVISO", "Z", mismaFecha),
                new ActividadNotificacionesPort.ActividadNotificacion(
                        UUID.fromString("00000000-0000-0000-0000-0000000000bb"), "AVISO", "A", mismaFecha)));

        var actividad = new ConsultarActividadRecienteService(servicios, notificaciones).ejecutar(usuario);

        assertEquals(List.of("AVISO: Z", "AVISO: A"), actividad.stream().map(a -> a.resumen()).toList());
    }

    private ServicioContratado servicio(EstadoServicio estado) {
        return new ServicioContratado(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), usuario, estado, true, OffsetDateTime.now(), 0, List.of(), List.of());
    }
}
