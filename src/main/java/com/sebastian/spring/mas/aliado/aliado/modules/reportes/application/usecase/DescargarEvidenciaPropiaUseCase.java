package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;

public interface DescargarEvidenciaPropiaUseCase {
    EvidenciaContenidoResult ejecutar(DescargarEvidenciaPropiaCommand command);
}
