package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.VerificacionAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.DocumentoVerificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.SolicitudVerificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.OperacionesVerificacion;
@Service
@Transactional
public class VerificacionService extends OperacionesVerificacion implements VerificacionAdminPort {
    public VerificacionService(SolicitudVerificacionRepository verificaciones,
                               DocumentoVerificacionRepository documentos, ArchivoStorageService storage) {
        super(verificaciones,documentos,storage);
    }
}
