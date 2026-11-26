package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service;
import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.NotificacionConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
public class ConsultarNotificacionesService implements ConsultarNotificacionesUseCase {
    private final NotificacionConsultaPort notificacionConsultaPort;
    public ConsultarNotificacionesService(NotificacionConsultaPort notificacionConsultaPort) { this.notificacionConsultaPort = notificacionConsultaPort; }
    @Override public List<NotificacionResult> ejecutar(ConsultarNotificacionesCommand command) {
        var tipo = command.tipo();
        List<NotificacionConsultaPort.NotificacionConsulta> datos;
        if (tipo == null || tipo.isBlank()) {
            datos = notificacionConsultaPort.listarPropias(command.destinatarioId());
        } else {
            TipoNotificacion valor;
            try { valor = TipoNotificacion.valueOf(tipo.trim()); }
            catch (IllegalArgumentException ex) {
                throw new BusinessException.BadRequest("TIPO_INVALIDO", "Tipo de notificación no reconocido.");
            }
            datos = notificacionConsultaPort.listarPorTipo(command.destinatarioId(), valor);
        }
        return datos.stream().map(n -> new NotificacionResult(n.id(), n.tipo().name(), n.servicioId(),
                n.solicitudId(), n.detalle(), n.estadoEntrega().name(), n.fechaGeneracion())).toList();
    }
}
