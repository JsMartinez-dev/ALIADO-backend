package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Lectura masiva de los avisos pendientes de una cuenta (F09-BANDEJA-02).
 *
 * <p>Una única sentencia filtra por destinatario y {@code leida_en IS NULL}, de modo que
 * solo afecta a los pendientes de esa cuenta y no toca los avisos que se publiquen después
 * de la actualización. Una cuenta sin pendientes, o repetir la operación, termina con
 * éxito sin filas ajenas.</p>
 */
public class MarcarTodasLeidasService implements MarcarTodasLeidasUseCase {

    private final NotificacionRepository notificaciones;
    private final Clock reloj;

    public MarcarTodasLeidasService(NotificacionRepository notificaciones, Clock reloj) {
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public void ejecutar(MarcarTodasLeidasCommand command) {
        notificaciones.marcarTodasLeidas(command.destinatarioId(), OffsetDateTime.now(reloj));
    }
}
