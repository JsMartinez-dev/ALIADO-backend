package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;
import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.*;
public interface ConsultarNotificacionesUseCase {

    List<NotificacionResult> ejecutar(ConsultarNotificacionesCommand command);

}
