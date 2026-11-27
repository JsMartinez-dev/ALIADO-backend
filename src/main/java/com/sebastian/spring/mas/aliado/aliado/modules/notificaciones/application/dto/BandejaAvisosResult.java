package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import java.util.List;

/**
 * Página de la bandeja propia (F09-BANDEJA-03). {@code cursorSiguiente} es {@code null}
 * cuando no quedan avisos por consultar, incluida una bandeja vacía.
 */
public record BandejaAvisosResult(List<AvisoBandejaResult> items, String cursorSiguiente) {
    public BandejaAvisosResult {
        items = List.copyOf(items);
    }
}
