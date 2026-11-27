package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarAvisoLeidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Primera lectura de un aviso propio (F09-BANDEJA-02).
 *
 * <p>La pertenencia y la actualización se resuelven en una sola sentencia por ID y
 * destinatario, con la hora del servidor. La sentencia conserva la fecha ya guardada, de
 * modo que repetir la operación o ejecutarla concurrentemente no sustituye la primera
 * lectura. Cero filas afectadas significa aviso inexistente o ajeno: ambos producen el
 * mismo error de negocio sin consultar la existencia global. Un aviso propio ya leído es
 * un éxito.</p>
 */
public class MarcarAvisoLeidoService implements MarcarAvisoLeidoUseCase {

    private final NotificacionRepository notificaciones;
    private final Clock reloj;

    public MarcarAvisoLeidoService(NotificacionRepository notificaciones, Clock reloj) {
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public void ejecutar(MarcarAvisoLeidoCommand command) {
        boolean propia = notificaciones.marcarLeida(command.notificacionId(),
                command.destinatarioId(), OffsetDateTime.now(reloj));
        if (!propia) {
            throw new BusinessException.NotFound("NOTIFICACION_NO_ENCONTRADA",
                    "Notificación no encontrada.");
        }
    }
}
