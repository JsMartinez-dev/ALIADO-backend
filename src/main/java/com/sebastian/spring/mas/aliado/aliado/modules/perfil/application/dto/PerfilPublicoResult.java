package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.util.UUID;

/** La salida pública no contiene datos de contacto ni dirección exacta. */
public record PerfilPublicoResult(UUID usuarioId, String zonaAproximada) {}
