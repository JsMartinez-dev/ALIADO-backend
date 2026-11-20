package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port;



public interface ArchivoHistorialPort {
    String guardar(byte[] contenido);
    byte[] leer(String clave);
}
