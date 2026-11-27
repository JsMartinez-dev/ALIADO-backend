package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ConsultarFotoSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.DescargaFotoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.FotoNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.AccesoFotoOportunidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DescargarFotoSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
public class DescargarFotoSolicitudService implements DescargarFotoSolicitudUseCase {
    private final SolicitudRepository solicitudes;
    private final ArchivoFotoSolicitudPort archivos;
    private final AccesoFotoOportunidadPort accesoOportunidad;
    public DescargarFotoSolicitudService(SolicitudRepository solicitudes, ArchivoFotoSolicitudPort archivos,
                                         AccesoFotoOportunidadPort accesoOportunidad) {
        this.solicitudes = solicitudes; this.archivos = archivos; this.accesoOportunidad = accesoOportunidad;
    }
    @Override
    public DescargaFotoResult ejecutar(ConsultarFotoSolicitudCommand command) {
        var actual = solicitudes.porId(command.solicitudId()).orElseThrow(SolicitudNoEncontradaException::new);
        boolean esClientePropietario = actual.clienteId().equals(command.usuarioId())
                && "CLIENTE".equals(command.rol());
        boolean esPrestadorEnSolicitudPublicada = "PRESTADOR".equals(command.rol())
                && actual.estado() == EstadoSolicitud.PUBLICADA
                && accesoOportunidad.prestadorPuedeVer(actual.id(), command.usuarioId());
        if (!esClientePropietario && !esPrestadorEnSolicitudPublicada) {
            throw new SolicitudNoEncontradaException();
        }
        var foto = actual.fotos().stream().filter(f -> f.id().equals(command.fotoId()))
                .findFirst().orElseThrow(FotoNoEncontradaException::new);
        return new DescargaFotoResult(archivos.leer(foto.archivoKey()));
    }
}
