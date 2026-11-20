package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto;

import java.util.UUID;

public record CrearSolicitudCommand(UUID clienteId, UUID categoriaId, String descripcion,
                                    UUID zonaId, String urgencia, String estadoDeseado) {}
