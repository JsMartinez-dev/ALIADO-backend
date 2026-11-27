package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.util.UUID;

/**
 * Consulta de una página de la bandeja propia (F09-BANDEJA-03).
 *
 * <p>{@code destinatarioId} procede siempre de la sesión autenticada. Los filtros llegan
 * como texto para que la validación de valores y la conversión a los enums del módulo
 * ocurran en el caso de uso, igual que en la consulta existente. {@code limite} nulo usa
 * 20 y {@code cursor} nulo inicia la paginación.</p>
 */
public record ConsultarBandejaCommand(UUID destinatarioId, String estadoLectura, String tipo,
                                      Integer limite, String cursor) {}
