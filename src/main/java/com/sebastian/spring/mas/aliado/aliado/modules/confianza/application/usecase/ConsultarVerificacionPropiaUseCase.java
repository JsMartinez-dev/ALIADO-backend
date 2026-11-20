package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;

public interface ConsultarVerificacionPropiaUseCase {
    VerificacionResult ejecutar(ConsultarVerificacionPropiaCommand command);
}
