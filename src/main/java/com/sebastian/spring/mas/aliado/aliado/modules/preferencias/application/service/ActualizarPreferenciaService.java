package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ActualizarPreferenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActualizarPreferenciaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.TipoAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.PreferenciaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception.PreferenciasOperacionException;

public class ActualizarPreferenciaService implements ActualizarPreferenciaUseCase {
    private final PreferenciaRepository preferencias;

    public ActualizarPreferenciaService(PreferenciaRepository preferencias) {
        this.preferencias = preferencias;
    }

    @Override
    public PreferenciaResult ejecutar(ActualizarPreferenciaCommand command) {
        TipoAviso tipo;
        try {
            tipo = TipoAviso.valueOf(command.tipoAviso());
        } catch (IllegalArgumentException ex) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.DATOS_INVALIDOS, "TIPO_AVISO_INVALIDO", "Tipo de aviso no reconocido.");
        }
        String canal = command.canal();
        if (!"PUSH".equals(canal) && !"EMAIL".equals(canal)) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.DATOS_INVALIDOS, "CANAL_INVALIDO", "El canal debe ser PUSH o EMAIL.");
        }
        boolean configurable = tipo != TipoAviso.AVISO_SEGURIDAD_CUENTA;
        if (!configurable && !command.habilitada()) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.CONFLICTO, "AVISO_NO_CONFIGURABLE",
                    "El aviso de seguridad de cuenta no puede deshabilitarse.");
        }
        var guardada = preferencias.guardar(new PreferenciaNotificacion(command.usuarioId(), tipo, canal,
                command.habilitada(), configurable));
        return new PreferenciaResult(guardada.tipoAviso().name(), guardada.canal(),
                guardada.habilitada(), guardada.configurable());
    }
}
