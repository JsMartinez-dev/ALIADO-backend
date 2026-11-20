package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto;

import java.util.UUID;

public record HistorialQuery(UUID usuarioId, int pagina, int tamano) {}
