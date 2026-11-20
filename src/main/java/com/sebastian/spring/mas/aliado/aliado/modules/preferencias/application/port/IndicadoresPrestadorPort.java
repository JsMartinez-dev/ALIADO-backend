package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port;

import java.util.UUID;

public interface IndicadoresPrestadorPort {
    boolean esPrestador(UUID prestadorId);
    Reputacion reputacionDe(UUID prestadorId);

    record Reputacion(double valor, long cantidad) {}
}
