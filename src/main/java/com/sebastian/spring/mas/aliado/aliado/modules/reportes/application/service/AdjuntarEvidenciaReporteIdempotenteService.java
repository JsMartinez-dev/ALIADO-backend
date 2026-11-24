package com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.HuellaContenido;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotencyRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.AdjuntarEvidenciaIdempotenteCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.EvidenciaIdempotenteResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.EvidenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.usecase.AdjuntarEvidenciaReporteIdempotenteUseCase;
import java.util.UUID;

public class AdjuntarEvidenciaReporteIdempotenteService
        implements AdjuntarEvidenciaReporteIdempotenteUseCase {
    private final OperacionesReportes operaciones;
    private final IdempotencyRepository idempotencia;
    private final ObjectMapper objectMapper;

    public AdjuntarEvidenciaReporteIdempotenteService(OperacionesReportes operaciones,
                                                       IdempotencyRepository idempotencia,
                                                       ObjectMapper objectMapper) {
        this.operaciones = operaciones;
        this.idempotencia = idempotencia;
        this.objectMapper = objectMapper;
    }

    public static String operacion(UUID reporteId, UUID reportanteId) {
        return "reportes.evidencia:" + reporteId + ":" + reportanteId;
    }

    @Override
    public EvidenciaIdempotenteResult ejecutar(AdjuntarEvidenciaIdempotenteCommand command) {
        if (command.claveIdempotencia() == null) {
            return new EvidenciaIdempotenteResult(201, crear(command));
        }

        String operacion = operacion(command.reporteId(), command.reportanteId());
        String huella = HuellaContenido.calcular(command.nombreOriginal(), command.tipoContenido(),
                command.contenido());
        idempotencia.bloquear(command.claveIdempotencia(), operacion);

        var existente = idempotencia.buscarConHuella(command.claveIdempotencia(), operacion);
        if (existente.isPresent()) {
            var guardada = existente.get();
            if (guardada.huella() != null && !guardada.huella().equals(huella)) {
                throw new BusinessException.Conflict("IDEMPOTENCIA_CONFLICTO",
                        "La Idempotency-Key ya fue usada con un contenido distinto para este reporte.");
            }
            return new EvidenciaIdempotenteResult(guardada.statusCode(),
                    leerEvidencia(guardada.cuerpoJson()));
        }

        EvidenciaResult evidencia = crear(command);
        idempotencia.guardarConHuella(command.claveIdempotencia(), operacion, command.reportanteId(),
                201, objectMapper.valueToTree(evidencia).toString(), huella);
        return new EvidenciaIdempotenteResult(201, evidencia);
    }

    private EvidenciaResult crear(AdjuntarEvidenciaIdempotenteCommand command) {
        var evidencia = operaciones.agregarEvidencia(command.reporteId(), command.reportanteId(),
                command.nombreOriginal(), command.tipoContenido(), command.contenido());
        return new EvidenciaResult(evidencia.id(), evidencia.archivoKey(), evidencia.tipoContenido());
    }

    private EvidenciaResult leerEvidencia(String cuerpoJson) {
        try {
            return objectMapper.readValue(cuerpoJson, EvidenciaResult.class);
        } catch (Exception ex) {
            throw new IllegalStateException("La respuesta idempotente guardada no es válida.", ex);
        }
    }
}
