package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.time.LocalDate;
import java.util.UUID;
public record RegistrarExperienciaProfesionalCommand(UUID prestadorId, String descripcion,
        LocalDate fechaInicio, LocalDate fechaFin) {}
