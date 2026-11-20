package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.usecase.NotificarPropuestaRecibidaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository.PropuestaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.EstadoPropuesta;
import java.util.UUID;
/** UC044: no registra un aviso nuevo a partir de una propuesta ya inactiva. */
public class NotificarPropuestaRecibidaService implements NotificarPropuestaRecibidaUseCase {
    private final PropuestaRepository propuestas;
    private final ConsultaSolicitudPropuestaPort solicitudes;
    private final RegistroNotificacionPropuestaPort registro;
    public NotificarPropuestaRecibidaService(PropuestaRepository propuestas, ConsultaSolicitudPropuestaPort solicitudes,
                                            RegistroNotificacionPropuestaPort registro) {
        this.propuestas = propuestas; this.solicitudes = solicitudes; this.registro = registro;
    }
    @Override public void ejecutar(UUID propuestaId) {
        propuestas.porId(propuestaId).filter(p -> p.estado() == EstadoPropuesta.ACTIVA).ifPresent(p ->
                solicitudes.porId(p.solicitudId()).ifPresent(s -> registro.registrar(p.id(), s.clienteId())));
    }
}
