package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;

public interface NotificarAceptacionPropuestaUseCase { void ejecutar(Eventos.PropuestaAceptada evento); }
