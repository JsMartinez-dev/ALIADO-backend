package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.util.*;
import java.time.*;
import java.math.*;

public record ReportarResenaCommand(UUID resenaId, UUID reportanteId, String motivo) {}
