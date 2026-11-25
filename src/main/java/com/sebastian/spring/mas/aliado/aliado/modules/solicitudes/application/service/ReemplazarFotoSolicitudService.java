package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.FotoSolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.FotoNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.ReemplazarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.FotoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class ReemplazarFotoSolicitudService implements ReemplazarFotoSolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final ArchivoFotoSolicitudPort fotoSolicitudPort;
    public ReemplazarFotoSolicitudService(SolicitudRepository solicitudes, ArchivoFotoSolicitudPort archivos) {
        this.solicitudRepository = solicitudes; this.fotoSolicitudPort = archivos;
    }
    @Override
    public FotoSolicitudResult ejecutar(GuardarFotoSolicitudCommand command) {
        var actual = AccesoSolicitud.propiaAbierta(solicitudRepository, command.solicitudId(), command.usuarioId());
        var anterior = actual.fotos().stream().filter(f -> f.id().equals(command.fotoId()))
                .findFirst().orElseThrow(FotoNoEncontradaException::new);
        String clave = fotoSolicitudPort.guardar(command.nombreOriginal(), command.tipoContenido(), command.contenido());
        FotoSolicitud foto = new FotoSolicitud(anterior.id(), actual.id(), clave, anterior.orden());
        solicitudRepository.guardar(actual.conFotoReemplazada(foto));
        fotoSolicitudPort.eliminar(anterior.archivoKey());
        return new FotoSolicitudResult(foto.id(), foto.archivoKey(), foto.orden());
    }
}
