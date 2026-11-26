package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RetirarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RetirarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.HuellaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import java.time.OffsetDateTime;

/**
 * Baja en línea de la instalación al cerrar sesión (F09-PUSH-01/D-08).
 *
 * <p>Exige la prueba de posesión y la credencial del vínculo. Revoca únicamente la
 * generación propia que corresponde a esa credencial y responde sin cambios cuando no
 * hay un vínculo propio autorizado, de modo que repetir la baja sea seguro.</p>
 */
public class RetirarDispositivoPushService implements RetirarDispositivoPushUseCase {
    private final DispositivoPushRepository repositorio;

    public RetirarDispositivoPushService(DispositivoPushRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void ejecutar(RetirarDispositivoPushCommand command) {
        if (command == null || command.instalacionId() == null || command.usuarioId() == null
                || command.pruebaInstalacion() == null || command.pruebaInstalacion().isBlank()
                || command.credencialRevocacion() == null || command.credencialRevocacion().isBlank()) {
            throw new BusinessException.BadRequest("DATOS_INVALIDOS", "Datos inválidos.");
        }
        InstalacionPush instalacion = repositorio.buscarInstalacion(command.instalacionId()).orElse(null);
        if (instalacion == null) {
            return;
        }
        HuellaPush prueba = HuellaPush.de(command.pruebaInstalacion());
        if (!instalacion.coincidePrueba(prueba.valor())) {
            return;
        }
        HuellaPush credencial = HuellaPush.de(command.credencialRevocacion());
        repositorio.revocarVinculoPropio(command.instalacionId(), command.usuarioId(),
                credencial.valor(), OffsetDateTime.now());
    }
}
