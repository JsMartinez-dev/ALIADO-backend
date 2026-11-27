package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConfigurarDisponibilidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.CompromisoAgenda;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.Disponibilidad;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
public class ConfigurarDisponibilidadService implements ConfigurarDisponibilidadUseCase {
    private static final ZoneId MVP_ZONE = ZoneId.of("America/Bogota");
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
        ZonedDateTime inicio = compromiso.fechaHoraInicio().atZoneSameInstant(MVP_ZONE);
        ZonedDateTime fin = compromiso.fechaHoraFin().atZoneSameInstant(MVP_ZONE);
        ZonedDateTime cursor = inicio;

        while (cursor.isBefore(fin)) {
            LocalDate dia = cursor.toLocalDate();
            ZonedDateTime finDelDia = dia.plusDays(1).atStartOfDay(MVP_ZONE);
            ZonedDateTime finTramo = fin.isBefore(finDelDia) ? fin : finDelDia;
            int diaSemana = dia.getDayOfWeek().getValue();
            if (bloque.diaSemana() == null || bloque.diaSemana() == diaSemana) {
                LocalDateTime inicioTramo = cursor.toLocalDateTime();
                LocalDateTime finTramoLocal = finTramo.toLocalDateTime();
                LocalDateTime inicioBloque = dia.atTime(bloque.horaInicio());
                LocalDateTime finBloque = dia.atTime(bloque.horaFin());
                if (inicioBloque.isBefore(finTramoLocal) && inicioTramo.isBefore(finBloque)) {
                    return true;
                }
            }
            cursor = finTramo;
        }
        return false;
    }

}
