package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ConfianzaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaModeracionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ReputacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReportePort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.OperacionesConfianza;
@Service
@Transactional
public class ConfianzaService extends OperacionesConfianza implements ConfianzaConsultaPort, ResenaModeracionPort {
    public ConfianzaService(CalificacionRepository calificaciones, ResenaRepository resenas,
                            ReputacionRepository reputaciones,
                            SolicitudVerificacionRepository verificaciones,
                            ServicioLecturaPort servicios, ReportePort reportes,
                            RolConsultaPort roles) {
        super(calificaciones,resenas,reputaciones,verificaciones,servicios,reportes,roles);
    }
}
