package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto.LimpiezaArchivosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ArchivosPendientesEliminacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.usecase.LimpiarArchivosPendientesUseCase;
import java.util.UUID;

public class LimpiarArchivosPendientesService implements LimpiarArchivosPendientesUseCase {
    private final ArchivosPendientesEliminacionPort pendientes;
    private final ArchivoStorageService storage;

    public LimpiarArchivosPendientesService(ArchivosPendientesEliminacionPort pendientes,
                                            ArchivoStorageService storage) {
        this.pendientes = pendientes;
        this.storage = storage;
    }

    @Override
    public LimpiezaArchivosResult ejecutar(UUID operacionId) {
        int eliminados = 0;
        for (String clave : pendientes.pendientes()) {
            try {
                storage.eliminar(clave);
                pendientes.eliminar(clave);
                eliminados++;
            } catch (RuntimeException ex) {
                pendientes.registrarFallo(clave, ex.getMessage());
            }
        }
        long restantes = operacionId == null
                ? pendientes.contarPendientes()
                : pendientes.contarPendientes(operacionId);
        return new LimpiezaArchivosResult(eliminados, restantes);
    }
}
