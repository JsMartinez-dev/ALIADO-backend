package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record LevantarBloqueoCommand(UUID usuarioId, UUID bloqueoId, UUID administradorId) {}
