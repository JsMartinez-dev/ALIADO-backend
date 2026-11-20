package com.sebastian.spring.mas.aliado.aliado.common.storage;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Adaptador local de ArchivoStorageService para desarrollo y piloto.
 * Los objetos viven fuera del repositorio, bajo aliado.storage.dir.
 * P-04: el proveedor S3-compatible productivo aún no está elegido.
 */
@Service
public class LocalArchivoStorageAdapter implements ArchivoStorageService {
    private final Path raiz;

    public LocalArchivoStorageAdapter(@Value("${aliado.storage.dir:${user.home}/.aliado/uploads}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo crear el directorio de almacenamiento: " + directorio, ex);
        }
    }

    @Override
    public String guardar(String nombreOriginal, String tipoContenido, byte[] contenido) {
        String clave = UUID.randomUUID() + ArchivoValidacion.extensionSegura(nombreOriginal);
        try {
            Files.write(raiz.resolve(clave), contenido);
            return clave;
        } catch (IOException ex) {
            throw new BusinessException(500, "ERROR_ALMACENAMIENTO", "No se pudo guardar el archivo.");
        }
    }

    @Override
    public byte[] leer(String clave) {
        // La clave es opaca y la genera el sistema, pero nunca se confía en ella:
        // la ruta resuelta debe permanecer bajo la raíz de almacenamiento.
        Path objetivo = raiz.resolve(clave).normalize();
        if (!objetivo.startsWith(raiz)) {
            throw new BusinessException.NotFound("ARCHIVO_NO_ENCONTRADO", "El archivo no existe.");
        }
        try {
            return Files.readAllBytes(objetivo);
        } catch (IOException ex) {
            throw new BusinessException.NotFound("ARCHIVO_NO_ENCONTRADO", "El archivo no existe.");
        }
    }

    @Override
    public void eliminar(String clave) {
        try {
            Files.deleteIfExists(raiz.resolve(clave).normalize());
        } catch (IOException ignored) {
            // La eliminación es best-effort; el registro en BD ya no referencia la clave.
        }
    }
}
