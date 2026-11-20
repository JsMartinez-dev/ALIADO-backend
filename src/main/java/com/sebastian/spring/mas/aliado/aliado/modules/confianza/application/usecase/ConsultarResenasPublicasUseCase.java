package com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.usecase;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;

public interface ConsultarResenasPublicasUseCase {
    List<ResenaPublicaResult> ejecutar(ConsultarResenasPublicasCommand command);
}
