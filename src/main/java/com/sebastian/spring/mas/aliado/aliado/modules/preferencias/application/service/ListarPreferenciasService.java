package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ListarPreferenciasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.TipoAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.PreferenciaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.PreferenciaPredeterminada;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class ListarPreferenciasService implements ListarPreferenciasUseCase {
    private final PreferenciaRepository preferencias;

    public ListarPreferenciasService(PreferenciaRepository preferencias) {
        this.preferencias = preferencias;
    }

    @Override
    public List<PreferenciaResult> ejecutar(UUID usuarioId) {
        var guardadas = preferencias.porUsuario(usuarioId);
        List<PreferenciaResult> resultado = new ArrayList<>();
        for (TipoAviso tipo : TipoAviso.values()) {
            for (String canal : List.of("PUSH", "EMAIL")) {
                boolean configurable = tipo != TipoAviso.AVISO_SEGURIDAD_CUENTA;
                boolean habilitada = !configurable || guardadas.stream()
                        .filter(p -> p.tipoAviso() == tipo && p.canal().equals(canal))
                        .findFirst().map(PreferenciaNotificacion::habilitada)
                        // Sin fila guardada se aplica el mismo default que el encolado y la entrega.
                        .orElseGet(() -> PreferenciaPredeterminada.habilitada(tipo, canal));
                resultado.add(new PreferenciaResult(tipo.name(), canal, habilitada, configurable));
            }
        }
        return resultado;
    }
}
