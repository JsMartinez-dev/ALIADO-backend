package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.*;
import java.time.*;
import java.math.*;

public record ReportarServicioCommand(UUID objetivoId, UUID reportanteId, String motivo) {}
