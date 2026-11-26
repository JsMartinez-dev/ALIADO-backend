package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarAvisoOportunidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarAvisoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.TrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Encadenado transaccional al publicar una solicitud (SPEC-06/09, F09-PUSH-05).
 *
 * <p>El aviso de bandeja se inserta con la unicidad de V4
 * ({@code tipo + agregado + destinatario + versión}) y el trabajo push con la unicidad
 * de V9 ({@code aviso + canal}). Ambos viven en la transacción de publicación: un
 * rollback revierte las dos filas y la señal {@code pg_notify} que emite el adaptador.</p>
 *
 * <p>La preferencia se consulta solo para encolar: apagada, el aviso permanece en la
 * bandeja y no se crea trabajo. El encolado se limita al aviso creado en esta ejecución,
 * por lo que reprocesar un evento o volver a habilitar la preferencia no convierte
 * avisos históricos en push (F09-PUSH-01).</p>
 */
public class RegistrarAvisoOportunidadService implements RegistrarAvisoOportunidadUseCase {
    private final NotificacionRepository notificaciones;
    private final TrabajoPushRepository trabajos;
    private final ConsultaPreferenciasPort preferencias;
    private final Duration vigencia;

    public RegistrarAvisoOportunidadService(NotificacionRepository notificaciones,
                                            TrabajoPushRepository trabajos,
                                            ConsultaPreferenciasPort preferencias,
                                            Duration vigencia) {
        this.notificaciones = notificaciones;
        this.trabajos = trabajos;
        this.preferencias = preferencias;
        this.vigencia = vigencia;
    }

    @Override
    public void ejecutar(RegistrarAvisoOportunidadCommand command) {
        Notificacion aviso = new Notificacion(UUID.randomUUID(), command.prestadorId(),
                TipoNotificacion.NUEVA_OPORTUNIDAD, command.solicitudId(), command.version(),
                command.detalle(), Canal.PUSH, EstadoEntrega.PENDIENTE, 0, OffsetDateTime.now(), null);
        // La bandeja no depende del push. El aviso se conserva aunque la preferencia esté apagada.
        if (!notificaciones.insertarSiNoExiste(aviso)) {
            return; // Evento ya procesado: no duplica aviso ni trabajo.
        }
        if (preferencias.avisoHabilitado(command.prestadorId(), TipoNotificacion.NUEVA_OPORTUNIDAD.name(),
                Canal.PUSH.name())) {
            trabajos.encolarSiNoExiste(aviso.id(), vigencia);
        }
    }
}
