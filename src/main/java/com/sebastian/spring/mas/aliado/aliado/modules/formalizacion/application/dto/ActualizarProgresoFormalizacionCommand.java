package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto;

import java.util.UUID;

public record ActualizarProgresoFormalizacionCommand(UUID prestadorId, UUID rutaId, int valor) {}
