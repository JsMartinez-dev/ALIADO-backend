package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReporteAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReportePort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.EvidenciaReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.ReporteRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.OperacionesReportes;
@Service
@Transactional
public class ReporteService extends OperacionesReportes implements ReportePort, ReporteAdminPort {
    public ReporteService(ReporteRepository reportes, EvidenciaReporteRepository evidencias,
                          ResenaConsultaPort resenas, UsuarioConsultaPort usuarios,
                          ServicioLecturaPort servicios, ArchivoStorageService storage) {
        super(reportes,evidencias,resenas,usuarios,servicios,storage);
    }
}
