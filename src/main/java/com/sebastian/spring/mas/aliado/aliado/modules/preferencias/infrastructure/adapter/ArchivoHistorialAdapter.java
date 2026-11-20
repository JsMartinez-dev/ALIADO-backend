package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ArchivoHistorialPort;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ArchivoHistorialAdapter implements ArchivoHistorialPort {
    private static final Logger LOG = LoggerFactory.getLogger(ArchivoHistorialAdapter.class);
    private final ArchivoStorageService storage;

    public ArchivoHistorialAdapter(ArchivoStorageService storage) {
        this.storage = storage;
    }

    @Override
    public String guardar(byte[] contenido) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("La exportación requiere una transacción activa.");
        }
        String clave = storage.guardar("historial.csv", "text/csv", contenido);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        storage.eliminar(clave);
                    } catch (RuntimeException ex) {
                        LOG.warn("No se pudo limpiar un archivo de exportación revertida.", ex);
                    }
                }
            }
        });
        return clave;
    }

    @Override
    public byte[] leer(String clave) {
        return storage.leer(clave);
    }
}
