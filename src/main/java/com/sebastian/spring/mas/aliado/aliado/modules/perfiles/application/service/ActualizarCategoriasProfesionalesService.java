package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.ActualizarCategoriasProfesionalesCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCategoriasProfesionalesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
public class ActualizarCategoriasProfesionalesService implements ActualizarCategoriasProfesionalesUseCase {
    private final PerfilProfesionalRepository perfiles;
    private final ConsultaCatalogoPerfilPort catalogo;
    public ActualizarCategoriasProfesionalesService(PerfilProfesionalRepository perfiles, ConsultaCatalogoPerfilPort catalogo) {
        this.perfiles = perfiles;
        this.catalogo = catalogo;
    }
    @Override
    public PerfilProfesionalResult ejecutar(ActualizarCategoriasProfesionalesCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var categorias = CatalogoPerfilValidacion.validarActivos(command.categoriaIds(), "categoría", catalogo::categoriaActiva);
        return PerfilProfesionalResult.desde(perfiles.guardar(actual.conCategorias(categorias)));
    }
}
