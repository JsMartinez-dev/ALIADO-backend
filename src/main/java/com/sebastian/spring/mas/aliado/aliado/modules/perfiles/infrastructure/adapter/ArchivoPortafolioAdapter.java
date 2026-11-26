package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ArchivoPortafolioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class ArchivoPortafolioAdapter implements ArchivoPortafolioPort {
    private static final Logger log = LoggerFactory.getLogger(ArchivoPortafolioAdapter.class);
    private final ArchivoStorageService storage;

    public ArchivoPortafolioAdapter(ArchivoStorageService storage) { this.storage = storage; }

    @Override
    public String guardar(String nombreOriginal, String tipoContenido, byte[] contenido) {
        exigirTransaccion();
        ArchivoValidacion.validar(nombreOriginal, tipoContenido, contenido, ArchivoValidacion.DOCUMENTOS);
        String clave = storage.guardar(nombreOriginal, tipoContenido, contenido);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) { limpiar(clave); }
                // Ante estado desconocido se conserva el archivo: podría existir una referencia confirmada.
            }
        });
        return clave;
    }

    @Override
    public void eliminar(String clave) {
        exigirTransaccion();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { limpiar(clave); }
        });
    }

    private void exigirTransaccion() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("La escritura de evidencias requiere una transacción activa.");
        }
    }

    private void limpiar(String clave) {
        try {
            storage.eliminar(clave);
        } catch (RuntimeException ex) {
            // La BD ya confirmó/revirtió; un fallo de limpieza no debe informar rollback ficticio.
            log.error("No se pudo completar la limpieza de una evidencia de portafolio", ex);
        }
    }
}
