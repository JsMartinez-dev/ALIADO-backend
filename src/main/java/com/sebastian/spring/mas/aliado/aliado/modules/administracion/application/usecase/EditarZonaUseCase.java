package com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.AdminResult.*;
import java.time.*;
import java.util.*;

public interface EditarZonaUseCase {
  ZonaResponse ejecutar(EditarZonaCommand command);
}
