package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.adapter;

import java.util.*;
import org.springframework.stereotype.Component;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.application.port.PropuestaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ConsultaGestionPropuestasPort;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
@Component
public class GestionPropuestasAdapter implements ConsultaGestionPropuestasPort {private final PropuestaConsultaPort port; public GestionPropuestasAdapter(PropuestaConsultaPort port){this.port=port;} public Optional<Propuesta> porId(UUID id){return port.porId(id);} public List<Propuesta> activasDeSolicitud(UUID id){return port.activasDeSolicitud(id);} public void marcarAceptada(UUID id){port.marcarAceptada(id);} public void cerrarActivasDeSolicitud(UUID id,UUID excepto){port.cerrarActivasDeSolicitud(id,excepto);}}
