package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.ReportarUsuarioUseCase;

public class ReportarUsuarioService implements ReportarUsuarioUseCase {
    private final OperacionesReportes operaciones;
    public ReportarUsuarioService(OperacionesReportes operaciones) { this.operaciones = operaciones; }
    @Override
    public UUID ejecutar(ReportarUsuarioCommand command) {
        return operaciones.crearReporteUsuario(command.objetivoId(), command.reportanteId(), command.motivo());
    }

}
