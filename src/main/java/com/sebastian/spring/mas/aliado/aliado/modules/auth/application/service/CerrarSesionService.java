package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.CerrarSesionUseCase;

public class CerrarSesionService implements CerrarSesionUseCase {
    private final SesionPort sesiones;

    public CerrarSesionService(SesionPort sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public void ejecutar(String token) {
        if (!sesiones.cerrarSesion(token)) {
            throw new BusinessException.Unauthorized("SESION_INVALIDA", "La sesión no existe o ya fue cerrada.");
        }
    }
}
