package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.DescargarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargarHistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ArchivoHistorialPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.DescargaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.exception.PreferenciasOperacionException;

public class DescargarHistorialService implements DescargarHistorialUseCase {
    private final DescargaRepository descargas;
    private final ArchivoHistorialPort storage;

    public DescargarHistorialService(DescargaRepository descargas, ArchivoHistorialPort storage) {
        this.descargas = descargas;
        this.storage = storage;
    }

    @Override
    public byte[] ejecutar(DescargarHistorialQuery query) {
        var descarga = descargas.porId(query.descargaId())
                .filter(d -> d.usuarioId().equals(query.usuarioId()))
                .orElseThrow(() -> new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.NO_ENCONTRADO, "DESCARGA_NO_ENCONTRADA",
                        "Exportación no encontrada."));
        if (!"LISTA".equals(descarga.estado())) {
            throw new PreferenciasOperacionException(PreferenciasOperacionException.Tipo.CONFLICTO, "DESCARGA_NO_LISTA",
                    "La exportación aún no está lista para descargar.");
        }
        return storage.leer(descarga.archivoKey());
    }
}
