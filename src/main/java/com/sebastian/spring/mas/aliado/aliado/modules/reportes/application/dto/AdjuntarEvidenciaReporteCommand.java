package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.*;
import java.time.*;
import java.math.*;

public record AdjuntarEvidenciaReporteCommand(UUID reporteId, UUID reportanteId, String nombreOriginal, String tipoContenido, byte[] contenido) {}
