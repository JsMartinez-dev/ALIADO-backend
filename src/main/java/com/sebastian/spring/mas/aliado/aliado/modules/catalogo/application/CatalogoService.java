package com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application;

import com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoAdminPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Categoria;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.EstadoCatalogo;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.model.Zona;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.repository.CategoriaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.domain.repository.ZonaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoService implements CatalogoConsultaPort, CatalogoAdminPort {
    private final CategoriaRepository categoriasRepository;
    private final ZonaRepository zonasRepository;
    private final AuditoriaPort auditoriaPort;

    public CatalogoService(CategoriaRepository categoriasRepository, ZonaRepository zonasRepository,
            AuditoriaPort auditoriaPort) {
        this.categoriasRepository = categoriasRepository;
        this.zonasRepository = zonasRepository;
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    @Transactional
    public Categoria crearCategoria(UUID administradorId, String nombreCategoria) {
        validarNombre(nombreCategoria);
        if (categoriasRepository.porNombre(nombreCategoria.trim()).isPresent()) {
            throw new BusinessException.Conflict("CATEGORIA_DUPLICADA", "Ya existe una categoría con ese nombre.");
        }
        Categoria categoriaCreada = categoriasRepository.guardar(
                new Categoria(UUID.randomUUID(), nombreCategoria.trim(), EstadoCatalogo.ACTIVO));
        auditoriaPort.registrar(administradorId, "CATEGORY_CREATED", "CATEGORIA",
                categoriaCreada.id(), categoriaCreada.nombre());
        return categoriaCreada;
    }

    @Override
    @Transactional
    public Categoria actualizarCategoria(UUID administradorId, UUID categoriaId, String nombreCategoria,
            String estadoCategoria) {
        Categoria categoriaActual = categoriaPorIdOError(categoriaId);
        if (nombreCategoria != null && nombreCategoria.isBlank()) {
            throw new BusinessException.BadRequest("NOMBRE_REQUERIDO", "El nombre no puede estar vacío.");
        }
        String nombreCategoriaFinal = nombreCategoria == null ? categoriaActual.nombre() : nombreCategoria.trim();
        categoriasRepository.porNombre(nombreCategoriaFinal)
                .filter(categoriaExistente -> !categoriaExistente.id().equals(categoriaId))
                .ifPresent(categoriaExistente -> {
                    throw new BusinessException.Conflict("CATEGORIA_DUPLICADA",
                            "Ya existe otra categoría con ese nombre.");
                });
        EstadoCatalogo estadoCategoriaActualizado = estadoOActual(estadoCategoria, categoriaActual.estado());
        Categoria categoriaActualizada = categoriasRepository.guardar(
                new Categoria(categoriaId, nombreCategoriaFinal, estadoCategoriaActualizado));
        auditoriaPort.registrar(administradorId, "CATEGORY_UPDATED", "CATEGORIA",
                categoriaId, nombreCategoriaFinal);
        return categoriaActualizada;
    }

    /** Borrado lógico (SPEC-12): desactivar no rompe borradores ni coberturas existentes. */
    @Override
    @Transactional
    public void desactivarCategoria(UUID administradorId, UUID categoriaId) {
        Categoria categoriaActual = categoriaPorIdOError(categoriaId);
        categoriasRepository.guardar(
                new Categoria(categoriaId, categoriaActual.nombre(), EstadoCatalogo.INACTIVO));
        auditoriaPort.registrar(administradorId, "CATEGORY_DEACTIVATED", "CATEGORIA",
                categoriaId, categoriaActual.nombre());
    }

    @Override
    @Transactional
    public Zona crearZona(UUID administradorId, String nombreZona, String coberturaGeografica) {
        validarNombre(nombreZona);
        if (zonasRepository.porNombre(nombreZona.trim()).isPresent()) {
            throw new BusinessException.Conflict("ZONA_DUPLICADA", "Ya existe una zona con ese nombre.");
        }
        Zona zonaCreada = zonasRepository.guardar(
                new Zona(UUID.randomUUID(), nombreZona.trim(), coberturaGeografica, EstadoCatalogo.ACTIVO));
        auditoriaPort.registrar(administradorId, "ZONE_CREATED", "ZONA", zonaCreada.id(), zonaCreada.nombre());
        return zonaCreada;
    }

    @Override
    @Transactional
    public Zona actualizarZona(UUID administradorId, UUID zonaId, String nombreZona, String coberturaGeografica,
            String estadoZona) {
        Zona zonaActual = zonaPorIdOError(zonaId);
        if (nombreZona != null && nombreZona.isBlank()) {
            throw new BusinessException.BadRequest("NOMBRE_REQUERIDO", "El nombre no puede estar vacío.");
        }
        String nombreZonaFinal = nombreZona == null ? zonaActual.nombre() : nombreZona.trim();
        zonasRepository.porNombre(nombreZonaFinal)
                .filter(zonaExistente -> !zonaExistente.id().equals(zonaId))
                .ifPresent(zonaExistente -> {
                    throw new BusinessException.Conflict("ZONA_DUPLICADA",
                            "Ya existe otra zona con ese nombre.");
                });
        EstadoCatalogo estadoZonaActualizado = estadoOActual(estadoZona, zonaActual.estado());
        Zona zonaActualizada = zonasRepository.guardar(new Zona(zonaId, nombreZonaFinal,
                coberturaGeografica == null ? zonaActual.coberturaGeografica() : coberturaGeografica,
                estadoZonaActualizado));
        auditoriaPort.registrar(administradorId, "ZONE_UPDATED", "ZONA", zonaId, nombreZonaFinal);
        return zonaActualizada;
    }

    @Override
    @Transactional
    public void desactivarZona(UUID administradorId, UUID zonaId) {
        Zona zonaActual = zonaPorIdOError(zonaId);
        zonasRepository.guardar(
                new Zona(zonaId, zonaActual.nombre(), zonaActual.coberturaGeografica(), EstadoCatalogo.INACTIVO));
        auditoriaPort.registrar(administradorId, "ZONE_DEACTIVATED", "ZONA",
                zonaId, zonaActual.nombre());
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriasRepository.todas();
    }

    @Transactional(readOnly = true)
    public List<Zona> listarZonas() {
        return zonasRepository.todas();
    }

    private void validarNombre(String nombreCatalogo) {
        if (nombreCatalogo == null || nombreCatalogo.isBlank()) {
            throw new BusinessException.BadRequest("NOMBRE_REQUERIDO", "El nombre es obligatorio.");
        }
    }

    private Categoria categoriaPorIdOError(UUID categoriaId) {
        return categoriasRepository.porId(categoriaId)
                .orElseThrow(() -> new BusinessException.NotFound("CATEGORIA_NO_ENCONTRADA",
                        "Categoría no encontrada."));
    }

    private Zona zonaPorIdOError(UUID zonaId) {
        return zonasRepository.porId(zonaId)
                .orElseThrow(() -> new BusinessException.NotFound("ZONA_NO_ENCONTRADA", "Zona no encontrada."));
    }

    private EstadoCatalogo estadoOActual(String estadoSolicitado, EstadoCatalogo estadoActual) {
        if (estadoSolicitado == null) {
            return estadoActual;
        }
        try {
            return EstadoCatalogo.valueOf(estadoSolicitado);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException.BadRequest("ESTADO_INVALIDO",
                    "Estado inválido: use ACTIVO o INACTIVO.");
        }
    }

    @Override
    public Optional<Categoria> categoriaPorId(UUID categoriaId) { return categoriasRepository.porId(categoriaId); }

    @Override
    public Optional<Zona> zonaPorId(UUID zonaId) { return zonasRepository.porId(zonaId); }

    @Override
    public Optional<Categoria> categoriaPorNombre(String nombreCategoria) {
        return categoriasRepository.porNombre(nombreCategoria);
    }

    @Override
    public Optional<Zona> zonaPorNombre(String nombreZona) { return zonasRepository.porNombre(nombreZona); }

    @Override
    public List<Categoria> categoriasActivas() { return categoriasRepository.activas(); }

    @Override
    public List<Zona> zonasActivas() { return zonasRepository.activas(); }
}
