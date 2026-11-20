package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConfigurarDisponibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.CompromisoAgenda;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.Disponibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
public class ConfigurarDisponibilidadService implements ConfigurarDisponibilidadUseCase {
    private final PerfilProfesionalRepository perfiles;
    public ConfigurarDisponibilidadService(PerfilProfesionalRepository perfiles) { this.perfiles = perfiles; }
    @Override
    public PerfilProfesionalResult ejecutar(ConfigurarDisponibilidadCommand command) {
        var actual = perfiles.porPrestadorId(command.prestadorId()).orElseThrow(PerfilProfesionalNoEncontradoException::new);
        var bloques = command.bloques();
        if (bloques == null) { throw new DatosPerfilProfesionalInvalidosException("DISPONIBILIDAD_INVALIDA", "Los bloques de disponibilidad son obligatorios."); }
        for (DisponibilidadDatos bloque : bloques) {
            if (bloque == null || bloque.horaInicio() == null || bloque.horaFin() == null
                    || !bloque.horaFin().isAfter(bloque.horaInicio())) {
                throw new DatosPerfilProfesionalInvalidosException("DISPONIBILIDAD_INVALIDA",
                        "Cada bloque requiere hora de fin posterior a la de inicio.");
            }
            if (bloque.diaSemana() != null && (bloque.diaSemana() < 1 || bloque.diaSemana() > 7)) {
                throw new DatosPerfilProfesionalInvalidosException("DISPONIBILIDAD_INVALIDA",
                        "diaSemana debe estar entre 1 y 7.");
            }
            for (CompromisoAgenda compromiso : actual.compromisos()) {
                if (chocaConCompromiso(bloque, compromiso)) {
                    throw new ConflictoAgendaException(
                            "La disponibilidad entra en conflicto con un compromiso ya registrado.");
                }
            }
        }
        List<Disponibilidad> conIds = bloques.stream()
                .map(b -> new Disponibilidad(UUID.randomUUID(), actual.id(),
                        b.diaSemana(), b.horaInicio(), b.horaFin())).toList();
        return PerfilProfesionalResult.desde(perfiles.guardar(actual.conDisponibilidades(conIds)));
    }

    private boolean chocaConCompromiso(DisponibilidadDatos bloque, CompromisoAgenda compromiso) {
        int diaCompromiso = compromiso.fechaHoraInicio().getDayOfWeek().getValue();
        if (bloque.diaSemana() != null && bloque.diaSemana() != diaCompromiso) {
            return false;
        }
        LocalTime inicio = compromiso.fechaHoraInicio().toLocalTime();
        LocalTime fin = compromiso.fechaHoraFin().toLocalTime();
        return bloque.horaInicio().isBefore(fin) && inicio.isBefore(bloque.horaFin());
    }

}
