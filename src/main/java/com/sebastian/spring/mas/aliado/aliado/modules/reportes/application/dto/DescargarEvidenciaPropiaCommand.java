package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto;

import java.util.*;
import java.time.*;
import java.math.*;

public record DescargarEvidenciaPropiaCommand(UUID reporteId, UUID evidenciaId, UUID reportanteId) {}
