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


@Service
public class SolicitudService implements SolicitudConsultaPort {
    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudes) {
        this.solicitudRepository = solicitudes;
    }

    @Override
    public Optional<Solicitud> porId(UUID id) {
        return solicitudRepository.porId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> buscarPorTexto(String texto) {
        return solicitudRepository.buscarPorTexto(texto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> publicadas() {
        return solicitudRepository.publicadas();
    }

    @Override
    @Transactional
    public void marcarContratada(UUID solicitudId) {
        Solicitud actual = solicitudRepository.porId(solicitudId)
                .orElseThrow(() -> new BusinessException.NotFound("SOLICITUD_NO_ENCONTRADA",
                        "Solicitud no encontrada."));
        solicitudRepository.guardar(actual.transitarA(EstadoSolicitud.CONTRATADA, OffsetDateTime.now()));
    }
}
