package com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port;

public interface ArchivoFotoSolicitudPort {
    String guardar(String nombreOriginal, String tipoContenido, byte[] contenido);
    byte[] leer(String clave);
    void eliminar(String clave);
}
