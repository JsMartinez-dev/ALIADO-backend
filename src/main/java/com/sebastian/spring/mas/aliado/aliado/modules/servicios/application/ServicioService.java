package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.PropuestaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.EstadoPropuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.HistorialEstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.MensajeServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contratación y ciclo de vida del servicio (SPEC-08, UC045–UC056).
 * La aceptación es una sola operación consistente: crea el servicio, acepta la propuesta,
 * cierra el resto, marca la solicitud CONTRATADA y habilita la comunicación.
 */
@Service
public class ServicioService implements ServicioConsultaPort, ServicioLecturaPort {
    private final ServicioRepository servicios;
    private final PropuestaConsultaPort propuestas;
    private final SolicitudConsultaPort solicitudes;
    private final PublicadorEventos eventos;

    public ServicioService(ServicioRepository servicios, PropuestaConsultaPort propuestas,
                           SolicitudConsultaPort solicitudes, PublicadorEventos eventos) {
        this.servicios = servicios;
        this.propuestas = propuestas;
        this.solicitudes = solicitudes;
        this.eventos = eventos;
    }

    @Override
    public boolean tieneServicioActivo(UUID usuarioId) {
        return !servicios.activosDeUsuario(usuarioId).isEmpty();
    }

    @Override
    public Optional<ServicioContratado> porId(UUID id) {
        return servicios.porId(id);
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId) {
        return servicios.porUsuario(usuarioId);
    }

    @Override
    public List<ServicioContratado> porUsuario(UUID usuarioId, int pagina, int tamano) {
        return servicios.porUsuario(usuarioId, pagina, tamano);
    }

    @Override
    public boolean esParticipante(UUID servicioId, UUID usuarioId) {
        return servicios.porId(servicioId).map(s -> s.esParticipante(usuarioId)).orElse(false);
    }
}
