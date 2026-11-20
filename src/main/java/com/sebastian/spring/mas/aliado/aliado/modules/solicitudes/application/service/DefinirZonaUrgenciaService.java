package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DefinirZonaUrgenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ZonaUrgenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.ZonaUrgenciaInvalidasException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ConsultaZonaSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DefinirZonaUrgenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;

/** UC017 incluido en creación/publicación: valida sin persistir una solicitud parcial. */
public class DefinirZonaUrgenciaService implements DefinirZonaUrgenciaUseCase {
    private final ConsultaZonaSolicitudPort zonas;

    public DefinirZonaUrgenciaService(ConsultaZonaSolicitudPort zonas) {
        this.zonas = zonas;
    }

    @Override
    public ZonaUrgenciaResult ejecutar(DefinirZonaUrgenciaCommand command) {
        if (command.zonaId() != null) {
            if (!zonas.estaActiva(command.zonaId())) {
                throw new ZonaUrgenciaInvalidasException("ZONA_INVALIDA", "La zona no existe o no está activa.");
            }
        } else if (command.publicar()) {
            throw new ZonaUrgenciaInvalidasException("ZONA_INVALIDA",
                    "La zona es obligatoria para publicar la solicitud.");
        }
        Urgencia urgencia = null;
        if (command.urgencia() != null && !command.urgencia().isBlank()) {
            try {
                urgencia = Urgencia.valueOf(command.urgencia());
            } catch (IllegalArgumentException ex) {
                throw new ZonaUrgenciaInvalidasException("URGENCIA_INVALIDA",
                        "Urgencia inválida. Valores: BAJA, MEDIA, ALTA, URGENTE.");
            }
        }
        if (command.publicar() && urgencia == null) {
            throw new ZonaUrgenciaInvalidasException("URGENCIA_REQUERIDA",
                    "La urgencia es obligatoria para publicar la solicitud.");
        }
        return new ZonaUrgenciaResult(command.zonaId(), urgencia);
    }
}
