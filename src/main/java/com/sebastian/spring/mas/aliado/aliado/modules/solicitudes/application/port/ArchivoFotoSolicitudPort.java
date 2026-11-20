package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

/** Almacenamiento validado que coordina compensación con la transacción de solicitudes. */
public interface ArchivoFotoSolicitudPort {
    String guardar(String nombreOriginal, String tipoContenido, byte[] contenido);
    byte[] leer(String clave);
    void eliminar(String clave);
}
