package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.util.List;
public record FiltrarTableroCommand(List<Solicitud> compatibles, String categoria, String urgencia) {}
