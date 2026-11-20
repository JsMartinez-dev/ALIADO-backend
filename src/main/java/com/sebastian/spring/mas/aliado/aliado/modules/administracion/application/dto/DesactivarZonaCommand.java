package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record DesactivarZonaCommand(UUID administradorId, UUID id) {}
