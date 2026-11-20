package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.math.BigDecimal;

public record ConfianzaResult(BigDecimal reputacion, int cantidadCalificaciones, String nivelVerificacion) {}
