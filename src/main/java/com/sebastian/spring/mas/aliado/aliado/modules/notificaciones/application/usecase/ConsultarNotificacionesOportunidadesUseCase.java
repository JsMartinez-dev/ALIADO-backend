package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.NotificacionOportunidadResult;
import java.util.List;
import java.util.UUID;
public interface ConsultarNotificacionesOportunidadesUseCase {List<NotificacionOportunidadResult> ejecutar(UUID prestadorId);}
