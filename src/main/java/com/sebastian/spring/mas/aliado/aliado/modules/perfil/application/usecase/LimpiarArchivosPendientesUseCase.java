package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.LimpiezaArchivosResult;
import java.util.UUID;

public interface LimpiarArchivosPendientesUseCase {
    LimpiezaArchivosResult ejecutar(UUID operacionId);
}
