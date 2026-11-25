package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoValidacion;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.ArchivoFotoSolicitudPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class ArchivoFotoSolicitudAdapter implements ArchivoFotoSolicitudPort {
    private static final Logger log = LoggerFactory.getLogger(ArchivoFotoSolicitudAdapter.class);
    private final ArchivoStorageService storage;

    public ArchivoFotoSolicitudAdapter(ArchivoStorageService storage) { this.storage = storage; }

    @Override
    public String guardar(String nombreOriginal, String tipoContenido, byte[] contenido) {
        exigirTransaccion();
        ArchivoValidacion.validar(nombreOriginal, tipoContenido, contenido, ArchivoValidacion.IMAGENES);
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
    public byte[] leer(String clave) { return storage.leer(clave); }

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
            throw new IllegalStateException("La escritura de fotos requiere una transacción activa.");
        }
    }

    private void limpiar(String clave) {
        try {
            storage.eliminar(clave);
        } catch (RuntimeException ex) {
            // La BD ya confirmó/revirtió; un fallo de limpieza no debe informar rollback ficticio.
            log.error("No se pudo completar la limpieza de una foto de solicitud", ex);
        }
    }
}
