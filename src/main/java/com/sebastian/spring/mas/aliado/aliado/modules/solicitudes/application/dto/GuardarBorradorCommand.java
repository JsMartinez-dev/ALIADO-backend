package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;

/** Entrada parcial de UC019; su estado no puede convertirse en publicación. */
public record GuardarBorradorCommand(UUID clienteId, UUID categoriaId, String descripcion,
                                     UUID zonaId, String urgencia) {}
