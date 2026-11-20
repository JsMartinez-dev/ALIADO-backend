package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.CalcularCompatibilidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.usecase.CalcularCompatibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model.CriteriosCompatibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilVisibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Visibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.PerfilProfesionalConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compatibilidad y oportunidades (SPEC-06, UC034–UC038).
 * Sin fórmula ni puntaje: la coincidencia es booleana por categoría, zona y disponibilidad.
 * Sin datos suficientes, la solicitud es no evaluable y no se muestra como compatible.
 */
@Service
public class OportunidadService implements CompatibilidadPort {
    private final SolicitudConsultaPort solicitudes;
    private final PerfilProfesionalConsultaPort perfiles;
    private final PerfilVisibilidadPort visibilidad;
    private final CalcularCompatibilidadUseCase calcularCompatibilidad;

    public OportunidadService(SolicitudConsultaPort solicitudes, PerfilProfesionalConsultaPort perfiles,
                              CatalogoConsultaPort catalogo, PerfilVisibilidadPort visibilidad) {
        this(solicitudes, perfiles, catalogo, visibilidad, new CalcularCompatibilidadService());
    }

    @Autowired
    public OportunidadService(SolicitudConsultaPort solicitudes, PerfilProfesionalConsultaPort perfiles,
                              CatalogoConsultaPort catalogo, PerfilVisibilidadPort visibilidad,
                              CalcularCompatibilidadUseCase calcularCompatibilidad) {
        this.solicitudes = solicitudes;
        this.perfiles = perfiles;
        this.visibilidad = visibilidad;
        this.calcularCompatibilidad = calcularCompatibilidad;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esCompatible(UUID solicitudId, UUID prestadorId) {
        Optional<Solicitud> solicitud = solicitudes.porId(solicitudId);
        Optional<PerfilProfesional> perfil = perfiles.porPrestadorId(prestadorId);
        return solicitud.isPresent() && perfil.isPresent() && compatible(solicitud.get(), perfil.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> prestadoresCompatibles(UUID solicitudId) {
        Solicitud solicitud = solicitudes.porId(solicitudId).orElse(null);
        if (solicitud == null || solicitud.estado() != EstadoSolicitud.PUBLICADA) {
            return List.of();
        }
        return perfiles.visiblesPorCategoriaYZona(solicitud.categoriaId(), solicitud.zonaId()).stream()
                .filter(p -> compatible(solicitud, p))
                .map(PerfilProfesional::prestadorId).toList();
    }

    private boolean compatible(Solicitud solicitud, PerfilProfesional perfil) {
        boolean publicada = solicitud.estado() == EstadoSolicitud.PUBLICADA;
        boolean publico = perfil.estadoExposicion() == EstadoExposicion.PUBLICO;
        boolean privado = publicada && publico
                && visibilidad.preferenciaDe(perfil.prestadorId()) == Visibilidad.PRIVADO;
        return calcularCompatibilidad.ejecutar(new CriteriosCompatibilidad(publicada, publico, privado,
                solicitud.categoriaId(), solicitud.zonaId(), perfil.categoriaIds(), perfil.zonaIds(),
                !perfil.disponibilidades().isEmpty()));
    }

}
