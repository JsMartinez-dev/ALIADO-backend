package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarActividadRecienteUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.ActividadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ActividadNotificacionesPort;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class ConsultarActividadRecienteService implements ConsultarActividadRecienteUseCase {
    private final HistorialServiciosPort servicios;
    private final ActividadNotificacionesPort notificaciones;

    public ConsultarActividadRecienteService(HistorialServiciosPort servicios, ActividadNotificacionesPort notificaciones) {
        this.servicios = servicios;
        this.notificaciones = notificaciones;
    }

    @Override
    public List<ActividadResult> ejecutar(UUID usuarioId) {
        List<ActividadResult> actividades = new ArrayList<>();
        servicios.porUsuario(usuarioId).forEach(s -> actividades.add(new ActividadResult("SERVICIO",
                "Servicio " + s.estado().name().toLowerCase(Locale.ROOT), s.fechaContratacion(),
                "SERVICIO", s.id())));
        notificaciones.listarPropias(usuarioId).forEach(n -> actividades.add(
                new ActividadResult("NOTIFICACION", n.tipo() + ": " + n.detalle(), n.fechaGeneracion(),
                        "NOTIFICACION", n.id())));
        return actividades.stream()
                .sorted(Comparator.comparing(ActividadResult::fecha).reversed()
                        .thenComparing(ActividadResult::referenciaTipo)
                        .thenComparing(a -> a.referenciaId().toString()))
                .limit(20)
                .toList();
    }
}
