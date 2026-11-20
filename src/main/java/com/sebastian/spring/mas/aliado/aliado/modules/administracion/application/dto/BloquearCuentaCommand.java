package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record BloquearCuentaCommand(UUID usuarioId, UUID administradorId, String motivo) {}
