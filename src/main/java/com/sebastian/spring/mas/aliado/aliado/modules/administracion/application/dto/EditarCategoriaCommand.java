package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record EditarCategoriaCommand(UUID administradorId, UUID id, String nombre, String estado) {}
