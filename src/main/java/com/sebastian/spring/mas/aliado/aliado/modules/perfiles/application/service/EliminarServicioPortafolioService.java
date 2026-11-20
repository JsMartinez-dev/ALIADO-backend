package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EliminarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.ServicioPortafolioNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ArchivoPortafolioPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.EliminarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class EliminarServicioPortafolioService implements EliminarServicioPortafolioUseCase {
    private final PerfilProfesionalRepository perfiles;
    private final ArchivoPortafolioPort archivos;
    public EliminarServicioPortafolioService(PerfilProfesionalRepository perfiles, ArchivoPortafolioPort archivos) {
        this.perfiles = perfiles; this.archivos = archivos;
    }
    @Override
    public void ejecutar(EliminarServicioPortafolioCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var item = actual.portafolio().stream().filter(s -> s.id().equals(command.servicioId())).findFirst()
                .orElseThrow(ServicioPortafolioNoEncontradoException::new);
        perfiles.guardar(actual.sinPortafolio(command.servicioId()));
        if (item.evidenciaKey() != null) { archivos.eliminar(item.evidenciaKey()); }
    }
}
