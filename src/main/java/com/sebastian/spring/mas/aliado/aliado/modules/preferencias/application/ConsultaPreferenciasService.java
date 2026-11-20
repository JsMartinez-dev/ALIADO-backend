package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.TipoAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lectura de preferencias de notificación (SPEC-14, plan F14-T004).
 * Servicio dedicado y sin dependencias de otros servicios: rompería el ciclo
 * el procesamiento de avisos y la consulta de actividad reciente, que lee la bandeja.
 */
@Service
public class ConsultaPreferenciasService implements ConsultaPreferenciasPort {
    private final PreferenciaRepository preferencias;

    public ConsultaPreferenciasService(PreferenciaRepository preferencias) {
        this.preferencias = preferencias;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean avisoHabilitado(UUID usuarioId, String tipoAviso, String canal) {
        TipoAviso tipo;
        try {
            tipo = TipoAviso.valueOf(tipoAviso);
        } catch (IllegalArgumentException ex) {
            return true; // Tipo sin preferencia conocida: no bloquear el aviso.
        }
        if (tipo == TipoAviso.AVISO_SEGURIDAD_CUENTA) {
            return true; // No configurable: siempre se entrega.
        }
        return preferencias.porUsuarioTipoCanal(usuarioId, tipo.name(), canal)
                .map(p -> p.habilitada())
                .orElse(true); // Sin preferencia guardada: habilitado por defecto.
    }
}
