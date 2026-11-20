package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.CompromisoAgenda;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.OrigenCompromiso;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
public class GuardadoCompromisoAgenda {
 private final PerfilProfesionalRepository perfiles;
 public GuardadoCompromisoAgenda(PerfilProfesionalRepository perfiles) {this.perfiles=perfiles;}
    public PerfilProfesionalResult.CompromisoResult guardar(UUID prestadorId, UUID compromisoId,
                                               OffsetDateTime inicio, OffsetDateTime fin) {
        var actual = perfiles.porPrestadorId(prestadorId).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new DatosPerfilProfesionalInvalidosException("COMPROMISO_INVALIDO",
                    "El compromiso requiere fin posterior al inicio.");
        }
        if (compromisoId != null && actual.compromisos().stream().noneMatch(c -> c.id().equals(compromisoId))) {
            throw new CompromisoNoEncontradoException();
        }
        // Coherente con la regla disponibilidad/agenda (plan-05 T026): dos compromisos
        // no pueden solaparse; en actualización se excluye el propio compromiso.
        for (CompromisoAgenda otro : actual.compromisos()) {
            if (compromisoId != null && otro.id().equals(compromisoId)) {
                continue;
            }
            if (inicio.isBefore(otro.fechaHoraFin()) && otro.fechaHoraInicio().isBefore(fin)) {
                throw new ConflictoAgendaException(
                        "El compromiso se solapa con otro ya registrado.");
            }
        }
        CompromisoAgenda compromiso = new CompromisoAgenda(
                compromisoId == null ? UUID.randomUUID() : compromisoId, actual.id(),
                inicio, fin, OrigenCompromiso.MANUAL);
        perfiles.guardar(actual.conCompromiso(compromiso));
        return new PerfilProfesionalResult.CompromisoResult(compromiso.id(), compromiso.fechaHoraInicio(), compromiso.fechaHoraFin(), compromiso.origen().name());
    }

}
