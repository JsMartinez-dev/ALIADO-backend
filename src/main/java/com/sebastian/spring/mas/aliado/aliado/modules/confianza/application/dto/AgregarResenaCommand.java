package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto;

import java.util.*;
import java.time.*;
import java.math.*;

public record AgregarResenaCommand(UUID calificacionId, UUID clienteId, String texto) {}
