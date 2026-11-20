package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.IndicadoresPrestadorPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IndicadoresPrestadorAdapter implements IndicadoresPrestadorPort {
    private final RolConsultaPort roles;
    private final ConfianzaConsultaPort confianza;

    public IndicadoresPrestadorAdapter(RolConsultaPort roles, ConfianzaConsultaPort confianza) {
        this.roles = roles;
        this.confianza = confianza;
    }

    @Override
    public boolean esPrestador(UUID prestadorId) {
        return roles.esPrestador(prestadorId);
    }

    @Override
    public Reputacion reputacionDe(UUID prestadorId) {
        var reputacion = confianza.confianzaDe(prestadorId).reputacion();
        return new Reputacion(reputacion.valor().doubleValue(), reputacion.cantidad());
    }
}
