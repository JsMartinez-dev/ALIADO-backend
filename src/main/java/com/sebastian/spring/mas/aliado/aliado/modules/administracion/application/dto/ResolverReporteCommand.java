package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record ResolverReporteCommand(
    UUID reporteId, UUID administradorId, String resultado, String justificacion) {}
