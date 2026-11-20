package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port;

import java.util.UUID;

public interface ConsultaEliminacionCuentaPort {
  boolean tieneEliminacionConfirmada(UUID usuarioId);
}
