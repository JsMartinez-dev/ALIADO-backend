package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ArchivosPendientesEliminacionPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LimpiarArchivosPendientesServiceTest {
    private final UUID operacionA = UUID.randomUUID();
    private final UUID operacionB = UUID.randomUUID();

    @Test
    void falloDeStorageMantieneClavesPendientesYReintentaDespues() {
        var pendientes = new Repo(Map.of("a", operacionA, "b", operacionA));
        var storage = new Storage("a");
        var service = new LimpiarArchivosPendientesService(pendientes, storage);

        var fallo = service.ejecutar(operacionA);

        assertEquals(1, fallo.eliminados());
        assertEquals(1, fallo.pendientes());
        assertEquals(1, pendientes.fallos.size());
        assertTrue(pendientes.claves.containsKey("a"));
        assertTrue(storage.eliminadas.contains("b"));

        storage.failKey = null;
        var recuperado = service.ejecutar(operacionA);

        assertEquals(1, recuperado.eliminados());
        assertEquals(0, recuperado.pendientes());
        assertTrue(pendientes.claves.isEmpty());
        assertTrue(storage.eliminadas.contains("a"));
    }

    @Test
    void clavesDuplicadasYAusentesNoRompenLaLimpiezaIdempotente() {
        var pendientes = new Repo(Map.of("a", operacionA, "inexistente", operacionA));
        var storage = new Storage(null);
        var service = new LimpiarArchivosPendientesService(pendientes, storage);

        var result = service.ejecutar(operacionA);

        assertEquals(2, result.eliminados());
        assertEquals(0, result.pendientes());
        assertTrue(pendientes.claves.isEmpty());
        assertEquals(1, storage.eliminadas.stream().filter("a"::equals).count());
        assertEquals(1, storage.eliminadas.stream().filter("inexistente"::equals).count());
    }

    @Test
    void respuestaDeUnaOperacionNoIncluyePendientesDeOtraYLaColaGlobalSeLimpiaDespues() {
        var pendientes = new Repo(Map.of("a", operacionA, "b", operacionB));
        var storage = new Storage("a");
        var service = new LimpiarArchivosPendientesService(pendientes, storage);

        var respuestaA = service.ejecutar(operacionA);

        assertEquals(1, respuestaA.pendientes());
        assertTrue(pendientes.claves.containsKey("a"));
        assertTrue(!pendientes.claves.containsKey("b"));

        storage.failKey = null;
        var limpiezaGlobal = service.ejecutar(null);

        assertEquals(0, limpiezaGlobal.pendientes());
        assertTrue(pendientes.claves.isEmpty());
        assertEquals(1, respuestaA.pendientes());
    }

    private static final class Storage implements ArchivoStorageService {
        String failKey;
        final List<String> eliminadas = new ArrayList<>();

        Storage(String failKey) {
            this.failKey = failKey;
        }

        @Override
        public String guardar(String nombreOriginal, String tipoContenido, byte[] contenido) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] leer(String clave) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void eliminar(String clave) {
            if (clave.equals(failKey)) {
                throw new IllegalStateException("storage no disponible");
            }
            eliminadas.add(clave);
        }
    }

    private static final class Repo implements ArchivosPendientesEliminacionPort {
        final Map<String, UUID> claves = new HashMap<>();
        final List<String> fallos = new ArrayList<>();

        Repo(Map<String, UUID> claves) {
            this.claves.putAll(claves);
        }

        @Override
        public void encolar(String clave, UUID operacionId) {
            claves.putIfAbsent(clave, operacionId);
        }

        @Override
        public List<String> pendientes() {
            return List.copyOf(claves.keySet());
        }

        @Override
        public void eliminar(String clave) {
            claves.remove(clave);
        }

        @Override
        public void registrarFallo(String clave, String error) {
            fallos.add(clave);
        }

        @Override
        public long contarPendientes() {
            return claves.size();
        }

        @Override
        public long contarPendientes(UUID operacionId) {
            return claves.values().stream().filter(operacionId::equals).count();
        }
    }
}
