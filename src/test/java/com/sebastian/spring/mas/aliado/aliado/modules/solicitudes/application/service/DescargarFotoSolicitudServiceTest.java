package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.AccesoFotoOportunidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.FotoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DescargarFotoSolicitudServiceTest {
    private static final UUID SOLICITUD_ID = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID FOTO_ID = UUID.randomUUID();
    private static final UUID OTRO_USUARIO_ID = UUID.randomUUID();
    private static final byte[] CONTENIDO = new byte[] { 1, 2, 3 };

    private SolicitudRepository solicitudes;
    private ArchivoFotoSolicitudPort archivos;
    private AccesoFotoOportunidadPort accesoOportunidad;
    private DescargarFotoSolicitudService service;

    @BeforeEach
    void setUp() {
        solicitudes = mock(SolicitudRepository.class);
        archivos = mock(ArchivoFotoSolicitudPort.class);
        accesoOportunidad = mock(AccesoFotoOportunidadPort.class);
        service = new DescargarFotoSolicitudService(solicitudes, archivos, accesoOportunidad);
        when(archivos.leer("solicitudes/foto.jpg")).thenReturn(CONTENIDO);
    }

    @Test
    void clientePropietarioPuedeLeerLaFotoAunqueLaSolicitudEsteCerrada() {
        given(EstadoSolicitud.CONTRATADA);

        var result = service.ejecutar(new ConsultarFotoSolicitudCommand(
                SOLICITUD_ID, CLIENTE_ID, "CLIENTE", FOTO_ID));

        assertArrayEquals(CONTENIDO, result.contenido());
        verify(archivos).leer("solicitudes/foto.jpg");
    }

    @Test
    void prestadorPuedeLeerFotoMientrasLaSolicitudEstaPublicada() {
        given(EstadoSolicitud.PUBLICADA);
        when(accesoOportunidad.prestadorPuedeVer(SOLICITUD_ID, OTRO_USUARIO_ID)).thenReturn(true);

        var result = service.ejecutar(new ConsultarFotoSolicitudCommand(
                SOLICITUD_ID, OTRO_USUARIO_ID, "PRESTADOR", FOTO_ID));

        assertArrayEquals(CONTENIDO, result.contenido());
    }

    @Test
    void prestadorNoPuedeLeerFotoDeSolicitudCerrada() {
        given(EstadoSolicitud.CONTRATADA);

        assertThrows(SolicitudNoEncontradaException.class, () -> service.ejecutar(
                new ConsultarFotoSolicitudCommand(SOLICITUD_ID, OTRO_USUARIO_ID, "PRESTADOR", FOTO_ID)));
        verify(accesoOportunidad, never()).prestadorPuedeVer(SOLICITUD_ID, OTRO_USUARIO_ID);
    }

    @Test
    void clienteAjenoNoPuedeLeerFotoDeSolicitudPublicada() {
        given(EstadoSolicitud.PUBLICADA);

        assertThrows(SolicitudNoEncontradaException.class, () -> service.ejecutar(
                new ConsultarFotoSolicitudCommand(SOLICITUD_ID, OTRO_USUARIO_ID, "CLIENTE", FOTO_ID)));
    }

    @Test
    void prestadorIncompatibleNoPuedeLeerFotoAunqueConozcaLosIdentificadores() {
        given(EstadoSolicitud.PUBLICADA);
        when(accesoOportunidad.prestadorPuedeVer(SOLICITUD_ID, OTRO_USUARIO_ID)).thenReturn(false);

        assertThrows(SolicitudNoEncontradaException.class, () -> service.ejecutar(
                new ConsultarFotoSolicitudCommand(SOLICITUD_ID, OTRO_USUARIO_ID, "PRESTADOR", FOTO_ID)));
    }

    @Test
    void administradorNoPuedeLeerFotosDeSolicitudes() {
        given(EstadoSolicitud.PUBLICADA);

        assertThrows(SolicitudNoEncontradaException.class, () -> service.ejecutar(
                new ConsultarFotoSolicitudCommand(SOLICITUD_ID, OTRO_USUARIO_ID, "ADMINISTRADOR", FOTO_ID)));
    }

    private void given(EstadoSolicitud estado) {
        var ahora = OffsetDateTime.now();
        var foto = new FotoSolicitud(FOTO_ID, SOLICITUD_ID, "solicitudes/foto.jpg", 1);
        var solicitud = new Solicitud(SOLICITUD_ID, CLIENTE_ID, UUID.randomUUID(), UUID.randomUUID(), "Reparación",
                null, estado, ahora, ahora, 0, List.of(foto), List.of());
        when(solicitudes.porId(SOLICITUD_ID)).thenReturn(Optional.of(solicitud));
    }
}
