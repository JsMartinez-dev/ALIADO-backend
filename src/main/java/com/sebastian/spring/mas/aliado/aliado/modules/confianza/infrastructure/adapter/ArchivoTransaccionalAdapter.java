package com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArchivoTransaccionalAdapter implements ArchivoStorageService {
    private static final Logger log = LoggerFactory.getLogger(ArchivoTransaccionalAdapter.class);
    private final ArchivoStorageService storage;

    public ArchivoTransaccionalAdapter(ArchivoStorageService storage) {
        this.storage = storage;
    }

    @Override
    public String guardar(String nombre, String tipo, byte[] contenido) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("El archivo requiere una transacción de metadatos activa.");
        }
        String clave = storage.guardar(nombre, tipo, contenido);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        storage.eliminar(clave);
                    } catch (RuntimeException ex) {
                        log.error("No se pudo compensar un archivo al revertir metadatos", ex);
                    }
                }
            }
        });
        return clave;
    }

    @Override
    public byte[] leer(String clave) { return storage.leer(clave); }

    @Override
    public void eliminar(String clave) { storage.eliminar(clave); }
}
