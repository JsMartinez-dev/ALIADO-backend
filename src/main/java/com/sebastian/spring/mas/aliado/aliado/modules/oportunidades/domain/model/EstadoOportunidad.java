package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.domain.model;

/**
 * Estado mínimo observable por un Prestador que conserva un vínculo con la solicitud
 * (F09-PUSH-10).
 *
 * <p>{@code ABIERTA} exige publicación y acceso vigente; {@code CERRADA} solo se revela a
 * quien ya estaba vinculado por su aviso. No expone fechas, descripción ni datos del
 * Cliente.</p>
 */
public enum EstadoOportunidad {
    ABIERTA, CERRADA
}
