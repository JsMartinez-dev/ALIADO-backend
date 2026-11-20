package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Exportación del historial de servicios a CSV (SPEC-14). */
public final class HistorialCsvExporter {
    private final StringBuilder csv = new StringBuilder("servicio_id,estado,fecha_contratacion\n");

    public void agregar(List<ServicioContratado> servicios) {
        for (var s : servicios) {
            csv.append(s.id()).append(',').append(s.estado().name()).append(',')
                    .append(s.fechaContratacion()).append('\n');
        }
    }

    public byte[] contenido() {
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
