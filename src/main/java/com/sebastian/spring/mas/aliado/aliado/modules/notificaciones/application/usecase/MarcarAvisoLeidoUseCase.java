package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;

/**
 * Marca como leído un aviso propio y conserva su primera fecha de lectura
 * (F09-BANDEJA-02).
 */
public interface MarcarAvisoLeidoUseCase {

    void ejecutar(MarcarAvisoLeidoCommand command);
}
