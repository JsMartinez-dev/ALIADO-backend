package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoOportunidadAviso;
import java.util.UUID;

/**
 * Estado mínimo devuelto al destinatario del aviso (F09-PUSH-10).
 *
 * <p>{@code puedeCotizar} es una indicación de pantalla: solo una oportunidad abierta
 * permite intentar cotizar. La API de propuestas vuelve a comprobar el estado real y
 * conserva la decisión final.</p>
 */
public record OportunidadDesdeAvisoResult(UUID solicitudId, EstadoOportunidadAviso estado) {

    public boolean puedeCotizar() {
        return estado == EstadoOportunidadAviso.ABIERTA;
    }
}
