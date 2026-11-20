package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ExportarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.HistorialServiciosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ArchivoHistorialPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.DescargaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.DescargaHistorial;
import java.util.UUID;
import java.time.OffsetDateTime;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.HistorialCsvExporter;

public class ExportarHistorialService implements ExportarHistorialUseCase {
    private final HistorialServiciosPort servicios;
    private final DescargaRepository descargas;
    private final ArchivoHistorialPort storage;

    public ExportarHistorialService(HistorialServiciosPort servicios, DescargaRepository descargas, ArchivoHistorialPort storage) {
        this.servicios = servicios;
        this.descargas = descargas;
        this.storage = storage;
    }

    @Override
    public DescargaResult ejecutar(UUID usuarioId) {
        var csv = new HistorialCsvExporter();
        int pagina = 0;
        int tamano = 100;
        while (true) {
            var lote = servicios.porUsuario(usuarioId, pagina++, tamano);
            csv.agregar(lote);
            if (lote.size() < tamano) {
                break;
            }
        }
        String clave = storage.guardar(csv.contenido());
        var guardada = descargas.guardar(new DescargaHistorial(UUID.randomUUID(), usuarioId,
                OffsetDateTime.now(), clave, "LISTA"));
        return new DescargaResult(guardada.id(), guardada.archivoKey(), guardada.estado());
    }
}
