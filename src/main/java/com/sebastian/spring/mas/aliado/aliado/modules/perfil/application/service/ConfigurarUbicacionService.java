package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.ConfigurarUbicacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.PerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.DatosUbicacionInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConfigurarUbicacionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.ConsultarPerfilUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.Ubicacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.UbicacionRepository;

/** UC009: configura la ubicación del perfil sin alterar datos propios de los servicios. */
public class ConfigurarUbicacionService implements ConfigurarUbicacionUseCase {
    private final UbicacionRepository ubicaciones;
    private final ConsultarPerfilUseCase consultarPerfil;

    public ConfigurarUbicacionService(UbicacionRepository ubicaciones, ConsultarPerfilUseCase consultarPerfil) {
        this.ubicaciones = ubicaciones;
        this.consultarPerfil = consultarPerfil;
    }

    @Override
    public PerfilResult ejecutar(ConfigurarUbicacionCommand command) {
        String zona = command.zonaAproximada();
        String direccionExacta = command.direccionExacta();
        if (zona == null || zona.isBlank()) {
            throw new DatosUbicacionInvalidosException("ZONA_REQUERIDA", "La zona aproximada es obligatoria.");
        }
        if (zona.trim().length() > 100) {
            throw new DatosUbicacionInvalidosException("ZONA_MUY_LARGA", "La zona aproximada admite máximo 100 caracteres.");
        }
        if (direccionExacta != null && direccionExacta.length() > 500) {
            throw new DatosUbicacionInvalidosException("DIRECCION_MUY_LARGA", "La dirección exacta admite máximo 500 caracteres.");
        }
        Ubicacion actual = ubicaciones.porUsuarioId(command.usuarioId()).orElse(null);
        String direccion = direccionExacta != null ? direccionExacta
                : (actual == null ? null : actual.direccionExacta());
        ubicaciones.guardar(new Ubicacion(command.usuarioId(), zona.trim(), direccion,
                actual == null ? 0 : actual.version()));
        return consultarPerfil.ejecutar(command.usuarioId());
    }
}
