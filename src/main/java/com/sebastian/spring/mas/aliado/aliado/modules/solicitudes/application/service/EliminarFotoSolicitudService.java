package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.FotoNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.EliminarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class EliminarFotoSolicitudService implements EliminarFotoSolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final ArchivoFotoSolicitudPort fotoSolicitudPort;
    public EliminarFotoSolicitudService(SolicitudRepository solicitudes, ArchivoFotoSolicitudPort archivos) {
        this.solicitudRepository = solicitudes; this.fotoSolicitudPort = archivos;
    }
    @Override
    public void ejecutar(ConsultarFotoSolicitudCommand command) {
        var actual = AccesoSolicitud.propiaAbierta(solicitudRepository, command.solicitudId(), command.usuarioId());
        var foto = actual.fotos().stream().filter(f -> f.id().equals(command.fotoId()))
                .findFirst().orElseThrow(FotoNoEncontradaException::new);
        solicitudRepository.guardar(actual.sinFoto(foto.id()));
        fotoSolicitudPort.eliminar(foto.archivoKey());
    }
}
