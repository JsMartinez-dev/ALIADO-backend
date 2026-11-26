package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.AgregarServicioPortafolioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.PortafolioResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PortafolioInvalidoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ArchivoPortafolioPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.AgregarServicioPortafolioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.ServicioPortafolio;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class AgregarServicioPortafolioService implements AgregarServicioPortafolioUseCase {
    private final PerfilProfesionalRepository perfilProfesionalRepository;
    private final ArchivoPortafolioPort archivoPortafolioPort;
    public AgregarServicioPortafolioService(PerfilProfesionalRepository perfiles, ArchivoPortafolioPort archivos) {
        this.perfilProfesionalRepository = perfiles;
        this.archivoPortafolioPort = archivos;
    }
    @Override
    public PortafolioResult ejecutar(AgregarServicioPortafolioCommand command) {
        var actual = perfilProfesionalRepository.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        if (command.descripcion() == null || command.descripcion().isBlank()) { throw new PortafolioInvalidoException(); }
        String clave = null;
        if (command.contenido() != null && command.contenido().length > 0) {
            clave = archivoPortafolioPort.guardar(command.nombreArchivo(), command.tipoContenido(), command.contenido());
        }
        var item = new ServicioPortafolio(UUID.randomUUID(), actual.id(), command.descripcion().trim(), clave, OffsetDateTime.now());
        var lista = new ArrayList<>(actual.portafolio());
        lista.add(item);
        perfilProfesionalRepository.guardar(actual.conPortafolio(List.copyOf(lista)));
        return new PortafolioResult(item.id(), item.descripcion(), item.evidenciaKey(), item.fechaCreacion());
    }
}
