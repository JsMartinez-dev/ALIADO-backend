package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port;

import java.util.*;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
public interface ConsultaGestionPropuestasPort {Optional<Propuesta> porId(UUID id); List<Propuesta> activasDeSolicitud(UUID id); void marcarAceptada(UUID id); void cerrarActivasDeSolicitud(UUID id,UUID excepto);}
