package com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.web.SecurityUtils;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ListarPreferenciasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ActualizarPreferenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ExportarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.DescargarHistorialUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarIndicadoresPrestadorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ConsultarActividadRecienteUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.HistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.DescargarHistorialQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.mapper.PreferenciasPresentationMapper;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.presentation.dto.PreferenciasDtos;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Preferencias, historial, indicadores y actividad (SPEC-14). */
@RestController
@RequestMapping("/api/v1")
public class PreferenciasController {
    private final ListarPreferenciasUseCase listarPreferencias;
    private final ActualizarPreferenciaUseCase actualizarPreferencia;
    private final ConsultarHistorialUseCase historial;
    private final ExportarHistorialUseCase exportarHistorial;
    private final DescargarHistorialUseCase descargarExportacion;
    private final ConsultarIndicadoresPrestadorUseCase misIndicadores;
    private final ConsultarActividadRecienteUseCase actividadReciente;

    public PreferenciasController(
            ListarPreferenciasUseCase listarPreferencias,
            ActualizarPreferenciaUseCase actualizarPreferencia,
            ConsultarHistorialUseCase historial,
            ExportarHistorialUseCase exportarHistorial,
            DescargarHistorialUseCase descargarExportacion,
            ConsultarIndicadoresPrestadorUseCase misIndicadores,
            ConsultarActividadRecienteUseCase actividadReciente) {
        this.listarPreferencias = listarPreferencias;
        this.actualizarPreferencia = actualizarPreferencia;
        this.historial = historial;
        this.exportarHistorial = exportarHistorial;
        this.descargarExportacion = descargarExportacion;
        this.misIndicadores = misIndicadores;
        this.actividadReciente = actividadReciente;
    }

    @GetMapping("/preferencias/notificaciones")
    public ResponseEntity<List<PreferenciasDtos.PreferenciaResponse>> listar() {
        return ResponseEntity.ok(listarPreferencias.ejecutar(SecurityUtils.actual().id()).stream()
                .map(PreferenciasPresentationMapper::aResponse).toList());
    }

    @PutMapping("/preferencias/notificaciones")
    public ResponseEntity<PreferenciasDtos.PreferenciaResponse> actualizar(
            @Valid @RequestBody PreferenciasDtos.ActualizarPreferenciaRequest req) {
        return ResponseEntity.ok(PreferenciasPresentationMapper.aResponse(actualizarPreferencia.ejecutar(
                PreferenciasPresentationMapper.aCommand(SecurityUtils.actual().id(), req))));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<PreferenciasDtos.ServicioResumen>> historial(
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(historial.ejecutar(new HistorialQuery(SecurityUtils.actual().id(), pagina, tamano))
                .stream().map(PreferenciasPresentationMapper::aResponse).toList());
    }

    @PostMapping("/historial/exportaciones")
    public ResponseEntity<PreferenciasDtos.DescargaResponse> exportar() {
        return ResponseEntity.status(HttpStatus.CREATED).body(PreferenciasPresentationMapper.aResponse(
                exportarHistorial.ejecutar(SecurityUtils.actual().id())));
    }

    @GetMapping("/historial/exportaciones/{id}")
    public ResponseEntity<byte[]> descargarExportacion(@PathVariable UUID id) {
        byte[] contenido = descargarExportacion.ejecutar(new DescargarHistorialQuery(SecurityUtils.actual().id(), id));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historial.csv\"")
                .body(contenido);
    }

    @GetMapping("/prestadores/mis-indicadores")
    public ResponseEntity<PreferenciasDtos.IndicadoresResponse> misIndicadores() {
        return ResponseEntity.ok(PreferenciasPresentationMapper.aResponse(
                misIndicadores.ejecutar(SecurityUtils.actual().id())));
    }

    @GetMapping("/actividad-reciente")
    public ResponseEntity<List<PreferenciasDtos.ActividadResponse>> actividadReciente() {
        return ResponseEntity.ok(actividadReciente.ejecutar(SecurityUtils.actual().id()).stream()
                .map(PreferenciasPresentationMapper::aResponse).toList());
    }
}
