package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.FormalizacionResult.*;
import java.util.*;

public interface ConsultarEnlaceInstitucionalUseCase {
  EnlaceResult ejecutar(ConsultarEnlaceInstitucionalCommand command);
}
