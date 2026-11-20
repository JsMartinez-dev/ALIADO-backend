package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.util.UUID;

/** Datos del perfil propio; independiente del transporte HTTP y de la persistencia. */
public record PerfilResult(UUID usuarioId, String nombre, String telefono, String preferenciaVisibilidad,
                           String zonaAproximada, String direccionExacta, int version) {}
