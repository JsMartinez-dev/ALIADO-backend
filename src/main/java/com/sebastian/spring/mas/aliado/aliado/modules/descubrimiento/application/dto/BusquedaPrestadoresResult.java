package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.dto;

import java.util.List;

public record BusquedaPrestadoresResult(List<PrestadorPublicoResult> prestadores, List<String> sugerencias) {}

