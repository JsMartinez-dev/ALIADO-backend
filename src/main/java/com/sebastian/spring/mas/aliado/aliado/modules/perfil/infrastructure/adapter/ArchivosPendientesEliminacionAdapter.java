package com.sebastian.spring.mas.aliado.aliado.modules.perfil.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ArchivosPendientesEliminacionPort;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArchivosPendientesEliminacionAdapter implements ArchivosPendientesEliminacionPort {
    private final JdbcTemplate jdbc;

    public ArchivosPendientesEliminacionAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void encolar(String clave, UUID operacionId) {
        jdbc.update("""
                insert into archivos_pendientes_eliminacion (clave, operacion_id, fecha_creacion)
                values (?, ?, now())
                on conflict (clave) do nothing
                """, clave, operacionId);
    }

    @Override
    public List<String> pendientes() {
        return jdbc.queryForList(
                "select clave from archivos_pendientes_eliminacion order by fecha_creacion, clave",
                String.class);
    }

    @Override
    public void eliminar(String clave) {
        jdbc.update("delete from archivos_pendientes_eliminacion where clave = ?", clave);
    }

    @Override
    public void registrarFallo(String clave, String error) {
        jdbc.update("""
                update archivos_pendientes_eliminacion
                set intentos = intentos + 1, ultimo_error = ?, fecha_ultimo_intento = now()
                where clave = ?
                """, error == null ? "ERROR_STORAGE" : error.substring(0, Math.min(error.length(), 1000)), clave);
    }

    @Override
    public long contarPendientes() {
        Long count = jdbc.queryForObject("select count(*) from archivos_pendientes_eliminacion", Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public long contarPendientes(UUID operacionId) {
        Long count = jdbc.queryForObject(
                "select count(*) from archivos_pendientes_eliminacion where operacion_id = ?",
                Long.class, operacionId);
        return count == null ? 0 : count;
    }
}
