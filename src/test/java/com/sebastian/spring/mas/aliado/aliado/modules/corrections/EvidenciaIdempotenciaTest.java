package com.sebastian.spring.mas.aliado.aliado.modules.corrections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.HuellaContenido;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotencyRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.AdjuntarEvidenciaIdempotenteCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.EvidenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.AdjuntarEvidenciaReporteIdempotenteService;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.OperacionesReportes;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.EvidenciaReporte;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenciaIdempotenciaTest {
    @Mock OperacionesReportes operaciones;
    @Mock IdempotencyRepository idempotencia;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mismaClaveYContenidoReproduceSinCrearOtraEvidencia() throws Exception {
        var reporteId = UUID.randomUUID();
        var usuarioId = UUID.randomUUID();
        var clave = UUID.randomUUID();
        var evidencia = new EvidenciaResult(UUID.randomUUID(), "evidencia.png", "image/png");
        byte[] contenido = {1, 2, 3};
        String huella = HuellaContenido.calcular("evidencia.png", "image/png", contenido);
        when(idempotencia.buscarConHuella(clave,
                AdjuntarEvidenciaReporteIdempotenteService.operacion(reporteId, usuarioId)))
                .thenReturn(Optional.of(new IdempotencyRepository.RespuestaConHuella(201,
                        objectMapper.writeValueAsString(evidencia), huella)));

        var resultado = service().ejecutar(new AdjuntarEvidenciaIdempotenteCommand(reporteId, usuarioId,
                "evidencia.png", "image/png", contenido, clave));

        assertEquals(201, resultado.statusCode());
        assertEquals(evidencia.id(), resultado.evidencia().id());
        verify(idempotencia).bloquear(clave,
                AdjuntarEvidenciaReporteIdempotenteService.operacion(reporteId, usuarioId));
        verifyNoInteractions(operaciones);
    }

    @Test
    void mismaClaveConContenidoDistintoEs409() {
        var reporteId = UUID.randomUUID();
        var usuarioId = UUID.randomUUID();
        var clave = UUID.randomUUID();
        when(idempotencia.buscarConHuella(clave,
                AdjuntarEvidenciaReporteIdempotenteService.operacion(reporteId, usuarioId)))
                .thenReturn(Optional.of(new IdempotencyRepository.RespuestaConHuella(201,
                        "{\"id\":\"00000000-0000-0000-0000-000000000000\"}", "otra-huella")));

        BusinessException ex = assertThrows(BusinessException.class, () -> service().ejecutar(
                new AdjuntarEvidenciaIdempotenteCommand(reporteId, usuarioId, "evidencia.png",
                        "image/png", new byte[]{1, 2, 3}, clave)));

        assertEquals(409, ex.status());
        assertEquals("IDEMPOTENCIA_CONFLICTO", ex.code());
        verifyNoInteractions(operaciones);
    }

    @Test
    void sinClaveCreaSinRegistrarIdempotencia() {
        var reporteId = UUID.randomUUID();
        var usuarioId = UUID.randomUUID();
        var evidencia = new EvidenciaReporte(UUID.randomUUID(), reporteId, "evidencia.png",
                "image/png", OffsetDateTime.now());
        when(operaciones.agregarEvidencia(eq(reporteId), eq(usuarioId), eq("evidencia.png"),
                eq("image/png"), any(byte[].class))).thenReturn(evidencia);

        var resultado = service().ejecutar(new AdjuntarEvidenciaIdempotenteCommand(reporteId, usuarioId,
                "evidencia.png", "image/png", new byte[]{1}, null));

        assertEquals(201, resultado.statusCode());
        assertEquals(evidencia.id(), resultado.evidencia().id());
        verifyNoInteractions(idempotencia);
    }

    @Test
    void claveNuevaPersisteHuellaYRespuesta() {
        var reporteId = UUID.randomUUID();
        var usuarioId = UUID.randomUUID();
        var clave = UUID.randomUUID();
        var evidencia = new EvidenciaReporte(UUID.randomUUID(), reporteId, "evidencia.png",
                "image/png", OffsetDateTime.now());
        when(idempotencia.buscarConHuella(any(), any())).thenReturn(Optional.empty());
        when(operaciones.agregarEvidencia(any(), any(), any(), any(), any())).thenReturn(evidencia);

        service().ejecutar(new AdjuntarEvidenciaIdempotenteCommand(reporteId, usuarioId,
                "evidencia.png", "image/png", new byte[]{1, 2, 3}, clave));

        ArgumentCaptor<String> huella = ArgumentCaptor.forClass(String.class);
        verify(idempotencia).guardarConHuella(eq(clave),
                eq(AdjuntarEvidenciaReporteIdempotenteService.operacion(reporteId, usuarioId)),
                eq(usuarioId), eq(201), anyString(), huella.capture());
        assertEquals(HuellaContenido.calcular("evidencia.png", "image/png", new byte[]{1, 2, 3}),
                huella.getValue());
    }

    @Test
    void huellaCambiaConBytesONombre() {
        assertNotEquals(HuellaContenido.calcular("a.png", "image/png", new byte[]{1}),
                HuellaContenido.calcular("a.png", "image/png", new byte[]{2}));
        assertNotEquals(HuellaContenido.calcular("a.png", "image/png", new byte[]{1}),
                HuellaContenido.calcular("b.png", "image/png", new byte[]{1}));
    }

    private AdjuntarEvidenciaReporteIdempotenteService service() {
        return new AdjuntarEvidenciaReporteIdempotenteService(operaciones, idempotencia, objectMapper);
    }
}
