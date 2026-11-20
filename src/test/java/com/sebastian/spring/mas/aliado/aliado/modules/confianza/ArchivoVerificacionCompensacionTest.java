package com.sebastian.spring.mas.aliado.aliado.modules.confianza;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.infrastructure.adapter.ArchivoTransaccionalAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.mockito.Mockito.*;

class ArchivoVerificacionCompensacionTest {
    @AfterEach void limpiarContexto() {
        TransactionSynchronizationManager.clear();
    }

    @Test void rollbackEliminaArchivoNuevo() {
        ArchivoStorageService storage = mock(ArchivoStorageService.class);
        byte[] contenido = new byte[]{1};
        when(storage.guardar("documento", "tipo", contenido)).thenReturn("clave");
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        new ArchivoTransaccionalAdapter(storage).guardar("documento", "tipo", contenido);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(storage).eliminar("clave");
    }

    @Test void commitConservaArchivoNuevo() {
        ArchivoStorageService storage = mock(ArchivoStorageService.class);
        byte[] contenido = new byte[]{1};
        when(storage.guardar("documento", "tipo", contenido)).thenReturn("clave");
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        new ArchivoTransaccionalAdapter(storage).guardar("documento", "tipo", contenido);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
        verify(storage, never()).eliminar(anyString());
    }
}
