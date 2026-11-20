package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto;

import java.time.*;
import java.util.*;

public record ResolverVerificacionAdministrativaCommand(
    UUID solicitudId, UUID administradorId, String estado, String justificacion) {}
