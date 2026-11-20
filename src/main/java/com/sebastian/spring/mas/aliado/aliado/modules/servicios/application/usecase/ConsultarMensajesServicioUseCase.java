package com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.usecase;

import java.util.List;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.dto.*;

public interface ConsultarMensajesServicioUseCase {List<MensajeServicioResult> ejecutar(AccionServicioCommand command);}
