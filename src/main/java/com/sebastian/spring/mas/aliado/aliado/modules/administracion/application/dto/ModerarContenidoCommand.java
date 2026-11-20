package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record ModerarContenidoCommand(
    UUID administradorId, String objetivoTipo, UUID objetivoId, String accion, String motivo) {}
