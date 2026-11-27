package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.repository;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropuestaRepository {
    Optional<Propuesta> porId(UUID id);
    Optional<Propuesta> activaDePrestadorEnSolicitud(UUID solicitudId, UUID prestadorId);
    List<Propuesta> activasDeSolicitud(UUID solicitudId);
    List<Propuesta> porPrestador(UUID prestadorId, int pagina, int tamano);
    List<Propuesta> porSolicitud(UUID solicitudId);
    Propuesta guardar(Propuesta propuesta);
}
