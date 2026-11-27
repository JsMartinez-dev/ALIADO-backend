package com.sebastian.spring.mas.aliado.aliado.modules.servicios.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.CuentaEliminadaPort;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CuentaEliminadaAdapter implements CuentaEliminadaPort {
    private final JdbcTemplate jdbc;

    public CuentaEliminadaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean eliminada(UUID usuarioId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from usuarios where id = ? and estado_cuenta = 'ELIMINADA'",
                Integer.class, usuarioId);
        return count != null && count > 0;
    }
}
