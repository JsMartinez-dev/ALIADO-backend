package com.sebastian.spring.mas.aliado.aliado.modules.common.storage;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.LocalArchivoStorageAdapter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalArchivoStorageAdapterTest {
    @Test
    void leeConRaizRelativaYAbsoluta() throws Exception {
        Path absoluta = Files.createTempDirectory("aliado-storage-abs");
        Path relativa = Path.of("target", "test-storage-" + UUID.randomUUID());
        try {
            assertLectura(new LocalArchivoStorageAdapter(absoluta.toString()));
            assertLectura(new LocalArchivoStorageAdapter(relativa.toString()));
        } finally {
            deleteTree(absoluta);
            deleteTree(Path.of("").toAbsolutePath().resolve(relativa));
        }
    }

    @Test
    void rechazaClaveQueSaleDeLaRaiz() throws Exception {
        Path absoluta = Files.createTempDirectory("aliado-storage-traversal");
        try {
            LocalArchivoStorageAdapter adapter = new LocalArchivoStorageAdapter(absoluta.toString());
            assertThrows(BusinessException.NotFound.class, () -> adapter.leer("../fuera.csv"));
        } finally {
            deleteTree(absoluta);
        }
    }

    private static void assertLectura(LocalArchivoStorageAdapter adapter) {
        byte[] contenido = "csv".getBytes(StandardCharsets.UTF_8);
        String clave = adapter.guardar("historial.csv", "text/csv", contenido);
        assertArrayEquals(contenido, adapter.leer(clave));
        adapter.eliminar(clave);
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (Exception ignored) {}
            });
        }
    }
}
