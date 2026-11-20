package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.ServiciosPropiosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.ConsultarServiciosPropiosUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import java.util.UUID;

public class ConsultarServiciosPropiosService implements ConsultarServiciosPropiosUseCase {
    private final ServicioRepository servicios;

    public ConsultarServiciosPropiosService(ServicioRepository servicios) {
        this.servicios = servicios;
    }

    @Override
    public ServiciosPropiosResult ejecutar(UUID usuarioId, int pagina, int tamano) {
        return ServiciosPropiosResult.desde(servicios.porUsuario(usuarioId, pagina, tamano), pagina, tamano,
                servicios.contarPorUsuario(usuarioId));
    }
}
