package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service;

import java.util.*;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.exception.ServicioOperacionException;
import com.sebastian.spring.mas.aliado.aliado.common.events.*;
public class CerrarPropuestasNoSeleccionadasService implements CerrarPropuestasNoSeleccionadasUseCase {
 private final ConsultaGestionPropuestasPort propuestas;public CerrarPropuestasNoSeleccionadasService(ConsultaGestionPropuestasPort p){propuestas=p;}
 public void ejecutar(CerrarPropuestasCommand c){propuestas.cerrarActivasDeSolicitud(c.solicitudId(),c.propuestaAceptadaId());}
}
