package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarCategoriasProfesionalesCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCategoriasProfesionalesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class ActualizarCategoriasProfesionalesService implements ActualizarCategoriasProfesionalesUseCase {
    private final PerfilProfesionalRepository perfilProfesionalRepository;
    private final ConsultaCatalogoPerfilPort consultaCatalogoPerfilPort;
    public ActualizarCategoriasProfesionalesService(PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo) {
        this.perfilProfesionalRepository = perfiles;
        this.consultaCatalogoPerfilPort = catalogo;
    }
    @Override
    public PerfilProfesionalResult ejecutar(ActualizarCategoriasProfesionalesCommand command) {
        var actual = perfilProfesionalRepository.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var categorias = CatalogoPerfilValidacion.validarActivos(command.categoriaIds(), "categoría", consultaCatalogoPerfilPort::categoriaActiva);
        return PerfilProfesionalResult.desde(perfilProfesionalRepository.guardar(actual.conCategorias(categorias)));
    }
}
