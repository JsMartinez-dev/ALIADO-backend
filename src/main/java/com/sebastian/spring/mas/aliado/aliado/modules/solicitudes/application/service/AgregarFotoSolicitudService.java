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
    private final SolicitudRepository solicitudesRepository;
    private final ArchivoFotoSolicitudPort archivoFotoSolicitudPort;

    public AgregarFotoSolicitudService(SolicitudRepository solicitudesRepository,
            ArchivoFotoSolicitudPort archivoFotoSolicitudPort) {
        this.solicitudesRepository = solicitudesRepository;
        this.archivoFotoSolicitudPort = archivoFotoSolicitudPort;
    }

    @Override
    public FotoSolicitudResult ejecutar(GuardarFotoSolicitudCommand command) {
        var solicitudActual = AccesoSolicitud.propiaAbierta(
                solicitudesRepository, command.solicitudId(), command.usuarioId());
        if (solicitudActual.fotos().size() >= 10) {
            throw new DatosSolicitudInvalidosException("LIMITE_FOTOS", "La solicitud admite como máximo 10 fotos.");
        }
        String claveArchivoFoto = archivoFotoSolicitudPort.guardar(
                command.nombreOriginal(), command.tipoContenido(), command.contenido());
        int ordenFoto = solicitudActual.fotos().stream().mapToInt(FotoSolicitud::orden).max().orElse(0) + 1;
        FotoSolicitud fotoSolicitud =
                new FotoSolicitud(UUID.randomUUID(), solicitudActual.id(), claveArchivoFoto, ordenFoto);
        solicitudesRepository.guardar(solicitudActual.conFoto(fotoSolicitud));
        return new FotoSolicitudResult(
                fotoSolicitud.id(), fotoSolicitud.archivoKey(), fotoSolicitud.orden());
    }
}
