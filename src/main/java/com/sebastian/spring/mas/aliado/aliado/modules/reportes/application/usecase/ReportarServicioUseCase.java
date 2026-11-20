package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase;

import java.util.*;
import java.time.*;
import java.math.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;

public interface ReportarServicioUseCase {
    UUID ejecutar(ReportarServicioCommand command);
}
