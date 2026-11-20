package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargarHistorialQuery;

public interface DescargarHistorialUseCase {
    byte[] ejecutar(DescargarHistorialQuery query);
}
