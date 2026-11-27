package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port;

import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.NivelVerificacion;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ConfianzaConsultaPort {
    record ReputacionInfo(BigDecimal valor, int cantidad) {}
    record ConfianzaInfo(ReputacionInfo reputacion, NivelVerificacion nivelVerificacion) {}
    record ResenaPublica(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}

    ConfianzaInfo confianzaDe(UUID prestadorId);

    List<ResenaPublica> resenasVisiblesDe(UUID prestadorId);
}
