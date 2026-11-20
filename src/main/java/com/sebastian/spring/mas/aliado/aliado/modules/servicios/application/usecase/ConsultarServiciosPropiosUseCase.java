package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.ServiciosPropiosResult;
import java.util.UUID;

public interface ConsultarServiciosPropiosUseCase {
    ServiciosPropiosResult ejecutar(UUID usuarioId, int pagina, int tamano);
}
