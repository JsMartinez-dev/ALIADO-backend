package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EliminarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.ServicioPortafolioNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ArchivoPortafolioPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EliminarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class EliminarServicioPortafolioService implements EliminarServicioPortafolioUseCase {
    private final PerfilProfesionalRepository perfilProfesionalRepository;
    private final ArchivoPortafolioPort archivoPortafolioPort;
    public EliminarServicioPortafolioService(PerfilProfesionalRepository perfiles, ArchivoPortafolioPort archivos) {
        this.perfilProfesionalRepository = perfiles; this.archivoPortafolioPort = archivos;
    }
    @Override
    public void ejecutar(EliminarServicioPortafolioCommand command) {
        var actual = perfilProfesionalRepository.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var item = actual.portafolio().stream().filter(s -> s.id().equals(command.servicioId())).findFirst()
                .orElseThrow(ServicioPortafolioNoEncontradoException::new);
        perfilProfesionalRepository.guardar(actual.sinPortafolio(command.servicioId()));
        if (item.evidenciaKey() != null) { archivoPortafolioPort.eliminar(item.evidenciaKey()); }
    }
}
