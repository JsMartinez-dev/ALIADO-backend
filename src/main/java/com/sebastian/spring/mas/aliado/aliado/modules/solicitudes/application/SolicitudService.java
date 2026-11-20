package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de solicitudes de servicio (SPEC-04, UC016–UC023).
 * Zona y urgencia obligatorias al publicar (FR-017); el borrador no es visible para prestadores.
 * Al publicar se emite SolicitudPublicada: las notificaciones de oportunidad (SPEC-06)
 * se generan por evento, sin acoplar este módulo al de notificaciones.
 */
@Service
public class SolicitudService implements SolicitudConsultaPort {
    private final SolicitudRepository solicitudes;

    public SolicitudService(SolicitudRepository solicitudes) {
        this.solicitudes = solicitudes;
    }

    @Override
    public Optional<Solicitud> porId(UUID id) {
        return solicitudes.porId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarPorTexto(String texto) {
        return solicitudes.buscarPorTexto(texto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> publicadas() {
        return solicitudes.publicadas();
    }

    @Override
    @Transactional
    public void marcarContratada(UUID solicitudId) {
        Solicitud actual = solicitudes.porId(solicitudId)
                .orElseThrow(() -> new BusinessException.NotFound("SOLICITUD_NO_ENCONTRADA",
                        "Solicitud no encontrada."));
        solicitudes.guardar(actual.transitarA(EstadoSolicitud.CONTRATADA, OffsetDateTime.now()));
    }
}
