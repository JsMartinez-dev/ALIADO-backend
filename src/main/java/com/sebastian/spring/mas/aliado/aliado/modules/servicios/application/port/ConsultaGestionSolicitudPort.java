package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port;

import java.util.*;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
public interface ConsultaGestionSolicitudPort {Optional<Solicitud> porId(UUID id);void marcarContratada(UUID id);}
