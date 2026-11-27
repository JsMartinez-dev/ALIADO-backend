package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port;

import java.util.List;
import java.util.UUID;

public interface ArchivosPendientesEliminacionPort {
    void encolar(String clave, UUID operacionId);
    List<String> pendientes();
    void eliminar(String clave);
    void registrarFallo(String clave, String error);
    long contarPendientes();
    long contarPendientes(UUID operacionId);
}
