package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ResolverEstadoOportunidadQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.EstadoOportunidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * F09-PUSH-10: ABIERTA reutiliza la regla del detalle; CERRADA solo para una solicitud
 * cancelada o contratada; borrador, inexistente o sin acceso no revelan estado.
 */
class ResolverEstadoOportunidadServiceTest {

    private static final UUID SOLICITUD = UUID.randomUUID();
    private static final UUID PRESTADOR = UUID.randomUUID();
    private static final UUID CATEGORIA = UUID.randomUUID();
    private static final UUID ZONA = UUID.randomUUID();

    private ConsultaOportunidadesPort consultas;
    private EvaluacionCompatibilidad evaluacion;
    private ResolverEstadoOportunidadService service;

    @BeforeEach
    void preparar() {
        consultas = mock(ConsultaOportunidadesPort.class);
        evaluacion = mock(EvaluacionCompatibilidad.class);
        service = new ResolverEstadoOportunidadService(consultas, evaluacion);
    }

    @Test
    void solicitudPublicadaYCompatibleDevuelveAbierta() {
        Solicitud solicitud = solicitud(EstadoSolicitud.PUBLICADA);
        PerfilProfesional perfil = perfil();
        when(consultas.solicitudPorId(SOLICITUD)).thenReturn(Optional.of(solicitud));
        when(consultas.perfilPorPrestadorId(PRESTADOR)).thenReturn(Optional.of(perfil));
        when(evaluacion.compatible(solicitud, perfil)).thenReturn(true);

        assertEquals(Optional.of(EstadoOportunidad.ABIERTA), service.ejecutar(query()));
    }

    @Test
    void solicitudPublicadaSinCompatibilidadVigenteNoRevelaEstado() {
        Solicitud solicitud = solicitud(EstadoSolicitud.PUBLICADA);
        PerfilProfesional perfil = perfil();
        when(consultas.solicitudPorId(SOLICITUD)).thenReturn(Optional.of(solicitud));
        when(consultas.perfilPorPrestadorId(PRESTADOR)).thenReturn(Optional.of(perfil));
        when(evaluacion.compatible(solicitud, perfil)).thenReturn(false);

        assertTrue(service.ejecutar(query()).isEmpty());
    }

    @Test
    void solicitudPublicadaSinPerfilNoRevelaEstado() {
        when(consultas.solicitudPorId(SOLICITUD)).thenReturn(Optional.of(solicitud(EstadoSolicitud.PUBLICADA)));
        when(consultas.perfilPorPrestadorId(PRESTADOR)).thenReturn(Optional.empty());

        assertTrue(service.ejecutar(query()).isEmpty());
    }

    @Test
    void solicitudCanceladaYContratadaDevuelvenCerrada() {
        when(consultas.solicitudPorId(SOLICITUD))
                .thenReturn(Optional.of(solicitud(EstadoSolicitud.CANCELADA)));
        assertEquals(Optional.of(EstadoOportunidad.CERRADA), service.ejecutar(query()));

        when(consultas.solicitudPorId(SOLICITUD))
                .thenReturn(Optional.of(solicitud(EstadoSolicitud.CONTRATADA)));
        assertEquals(Optional.of(EstadoOportunidad.CERRADA), service.ejecutar(query()));
    }

    @Test
    void borradorInexistenteNoRevelanEstado() {
        when(consultas.solicitudPorId(SOLICITUD)).thenReturn(Optional.of(solicitud(EstadoSolicitud.BORRADOR)));
        assertTrue(service.ejecutar(query()).isEmpty());

        when(consultas.solicitudPorId(SOLICITUD)).thenReturn(Optional.empty());
        assertTrue(service.ejecutar(query()).isEmpty());
    }

    private ResolverEstadoOportunidadQuery query() {
        return new ResolverEstadoOportunidadQuery(SOLICITUD, PRESTADOR);
    }

    private Solicitud solicitud(EstadoSolicitud estado) {
        var ahora = OffsetDateTime.now();
        return new Solicitud(SOLICITUD, UUID.randomUUID(), CATEGORIA, ZONA, "Problema", Urgencia.MEDIA,
                estado, ahora, ahora, 0, List.of(), List.of());
    }

    private PerfilProfesional perfil() {
        var ahora = OffsetDateTime.now();
        return new PerfilProfesional(UUID.randomUUID(), PRESTADOR, Map.of(), EstadoExposicion.PUBLICO,
                ahora, ahora, Set.of(CATEGORIA), Set.of(ZONA), List.of(), List.of(), List.of(), List.of());
    }
}
