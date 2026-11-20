package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.PropuestaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.EstadoPropuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creación y gestión de propuestas (SPEC-07, UC039–UC044).
 * Solo sobre solicitud abierta y compatible; disponibilidad obligatoria; mensaje opcional.
 * Creación + notificación PROPUESTA_RECIBIDA en una sola transacción (vía evento).
 */
@Service
public class PropuestaService implements PropuestaConsultaPort {
    private final PropuestaRepository propuestas;
    private final SolicitudConsultaPort solicitudes;
    private final CompatibilidadPort compatibilidad;
    private final PublicadorEventos eventos;

    public PropuestaService(PropuestaRepository propuestas, SolicitudConsultaPort solicitudes,
                            CompatibilidadPort compatibilidad, PublicadorEventos eventos) {
        this.propuestas = propuestas;
        this.solicitudes = solicitudes;
        this.compatibilidad = compatibilidad;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<Propuesta> propuestasDeSolicitud(UUID solicitudId, UUID clienteId) {
        var solicitud = solicitudes.porId(solicitudId)
                .orElseThrow(() -> new BusinessException.NotFound("SOLICITUD_NO_ENCONTRADA",
                        "Solicitud no encontrada."));
        if (!solicitud.clienteId().equals(clienteId)) {
            throw new BusinessException.NotFound("SOLICITUD_NO_ENCONTRADA", "Solicitud no encontrada.");
        }
        return activasDeSolicitud(solicitudId);
    }

    @Override
    public Optional<Propuesta> porId(UUID id) {
        return propuestas.porId(id);
    }

    @Override
    public List<Propuesta> activasDeSolicitud(UUID solicitudId) {
        return propuestas.activasDeSolicitud(solicitudId);
    }

    @Override
    @Transactional
    public void marcarAceptada(UUID propuestaId) {
        Propuesta propuesta = propuestas.porId(propuestaId)
                .orElseThrow(() -> new BusinessException.NotFound("PROPUESTA_NO_ENCONTRADA",
                        "Propuesta no encontrada."));
        propuestas.guardar(propuesta.aceptar(OffsetDateTime.now()));
    }

    @Override
    @Transactional
    public void cerrarActivasDeSolicitud(UUID solicitudId, UUID exceptoPropuestaId) {
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Propuesta propuesta : propuestas.activasDeSolicitud(solicitudId)) {
            if (!propuesta.id().equals(exceptoPropuestaId)) {
                propuestas.guardar(propuesta.cerrar(ahora));
            }
        }
    }
}
