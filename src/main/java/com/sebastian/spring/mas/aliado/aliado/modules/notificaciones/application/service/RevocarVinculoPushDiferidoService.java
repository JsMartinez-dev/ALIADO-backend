package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RevocarVinculoPushDiferidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RevocarVinculoPushDiferidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.HuellaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import java.time.OffsetDateTime;

/**
 * Baja diferida sin sesión (F09-PUSH-01/D-08). La credencial opaca solo puede revocar la
 * generación activa que la registró; una credencial desconocida, retirada o repetida no
 * modifica la instalación y también responde como aplicada.
 */
public class RevocarVinculoPushDiferidoService implements RevocarVinculoPushDiferidoUseCase {
    private final DispositivoPushRepository repositorio;

    public RevocarVinculoPushDiferidoService(DispositivoPushRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void ejecutar(RevocarVinculoPushDiferidoCommand command) {
        if (command == null || command.instalacionId() == null
                || command.credencialRevocacion() == null || command.credencialRevocacion().isBlank()) {
            throw new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
        }
        HuellaPush credencial = HuellaPush.de(command.credencialRevocacion());
        repositorio.revocarVinculoPorCredencial(command.instalacionId(), credencial.valor(),
                OffsetDateTime.now());
    }
}
