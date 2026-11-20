package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port;
public interface ArchivoPortafolioPort {
    String guardar(String nombreOriginal, String tipoContenido, byte[] contenido);
    void eliminar(String clave);
}
