package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.FotoSolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.AgregarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.FotoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class AgregarFotoSolicitudService implements AgregarFotoSolicitudUseCase {
    private final SolicitudRepository solicitudes;
    private final ArchivoFotoSolicitudPort archivos;
    public AgregarFotoSolicitudService(SolicitudRepository solicitudes, ArchivoFotoSolicitudPort archivos) {
        this.solicitudes = solicitudes; this.archivos = archivos;
    }
    @Override
    public FotoSolicitudResult ejecutar(GuardarFotoSolicitudCommand command) {
        var actual = AccesoSolicitud.propiaAbierta(solicitudes, command.solicitudId(), command.usuarioId());
        if (actual.fotos().size() >= 10) {
            throw new DatosSolicitudInvalidosException("LIMITE_FOTOS", "La solicitud admite como máximo 10 fotos.");
        }
        String clave = archivos.guardar(command.nombreOriginal(), command.tipoContenido(), command.contenido());
        int orden = actual.fotos().stream().mapToInt(FotoSolicitud::orden).max().orElse(0) + 1;
        FotoSolicitud foto = new FotoSolicitud(UUID.randomUUID(), actual.id(), clave, orden);
        solicitudes.guardar(actual.conFoto(foto));
        return new FotoSolicitudResult(foto.id(), foto.archivoKey(), foto.orden());
    }
}
