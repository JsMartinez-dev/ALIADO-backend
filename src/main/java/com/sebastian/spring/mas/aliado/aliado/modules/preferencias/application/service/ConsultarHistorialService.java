package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.HistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ServicioResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception.PreferenciasOperacionException;

public class ConsultarHistorialService implements ConsultarHistorialUseCase {
    private final HistorialServiciosPort servicios;

    public ConsultarHistorialService(HistorialServiciosPort servicios) {
        this.servicios = servicios;
    }

    @Override
    public List<ServicioResult> ejecutar(HistorialQuery query) {
        if (query.pagina() < 0 || query.tamano() < 1 || query.tamano() > 100) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.DATOS_INVALIDOS, "PAGINACION_INVALIDA",
                    "pagina debe ser >= 0 y tamano debe estar entre 1 y 100.");
        }
        return servicios.porUsuario(query.usuarioId(), query.pagina(), query.tamano()).stream()
                .map(s -> new ServicioResult(s.id(), s.estado().name(), s.fechaContratacion()))
                .toList();
    }
}
