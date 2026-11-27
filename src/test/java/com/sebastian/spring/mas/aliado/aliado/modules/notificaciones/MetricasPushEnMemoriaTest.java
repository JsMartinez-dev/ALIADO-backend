package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResumenMetricasPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.MetricasPushEnMemoria;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-11: métricas del ciclo push sin lecturas periódicas de la cola y sin datos
 * sensibles. Los contadores distinguen encolado, reclamación, reintento, aceptación y
 * fallo; el tamaño de la cola solo aparece cuando el worker cierra un lote con trabajo.
 */
class MetricasPushEnMemoriaTest {

    private static final Instant AHORA = Instant.parse("2026-03-01T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    @Test
    void cadaEtapaDelCicloSeCuentaPorSeparado() {
        MetricasPushEnMemoria metricas = new MetricasPushEnMemoria(RELOJ);

        metricas.encolado(TipoNotificacion.NUEVA_OPORTUNIDAD);
        metricas.reclamacion(TipoReclamacionPush.PREPARACION, 1);
        metricas.reclamacion(TipoReclamacionPush.ENVIO, 2);
        metricas.reintento(TipoNotificacion.NUEVA_OPORTUNIDAD, 1);
        metricas.aceptacion(TipoNotificacion.NUEVA_OPORTUNIDAD, Duration.ofMillis(1_500));
        metricas.fallo("PREFERENCIA_DESHABILITADA");

        ResumenMetricasPush resumen = metricas.resumen();
        assertEquals(Map.of("NUEVA_OPORTUNIDAD", 1L), resumen.encolados());
        assertEquals(Map.of("ENVIO", 2L, "PREPARACION", 1L), resumen.reclamaciones());
        assertEquals(Map.of("NUEVA_OPORTUNIDAD", 1L), resumen.reintentos());
        assertEquals(Map.of("NUEVA_OPORTUNIDAD", 1L), resumen.aceptaciones());
        assertEquals(Map.of("PREFERENCIA_DESHABILITADA", 1L), resumen.fallos());
    }

    @Test
    void laLatenciaSePromediaYSeConservaLaMaxima() {
        MetricasPushEnMemoria metricas = new MetricasPushEnMemoria(RELOJ);

        metricas.aceptacion(TipoNotificacion.NUEVA_OPORTUNIDAD, Duration.ofMillis(1_000));
        metricas.aceptacion(TipoNotificacion.NUEVA_OPORTUNIDAD, Duration.ofMillis(3_000));

        ResumenMetricasPush resumen = metricas.resumen();
        assertEquals(Duration.ofMillis(2_000), resumen.latenciaAceptacionMedia());
        assertEquals(Duration.ofMillis(3_000), resumen.latenciaAceptacionMaxima());
    }

    @Test
    void enReposoNoSeInventaUnTamanoDeCola() {
        MetricasPushEnMemoria metricas = new MetricasPushEnMemoria(RELOJ);

        metricas.encolado(TipoNotificacion.NUEVA_OPORTUNIDAD);
        ResumenMetricasPush resumen = metricas.resumen();

        assertEquals(0, resumen.trabajosAbiertos(), "sin lote procesado no se lee la cola");
        assertEquals(0, resumen.entregasPendientes());
        assertEquals(Duration.ZERO, resumen.antiguedadMaxima());
    }

    @Test
    void elCierreDeLoteRegistraTamanoYAntiguedadDeLaCola() {
        MetricasPushEnMemoria metricas = new MetricasPushEnMemoria(RELOJ);

        metricas.pendientes(new PendientesPush(3, 5, AHORA.minusSeconds(120)));
        ResumenMetricasPush resumen = metricas.resumen();

        assertEquals(3, resumen.trabajosAbiertos());
        assertEquals(5, resumen.entregasPendientes());
        assertEquals(Duration.ofSeconds(120), resumen.antiguedadMaxima());
    }

    @Test
    void elResumenOperativoNoIncluyeTokensNiInstalaciones() {
        MetricasPushEnMemoria metricas = new MetricasPushEnMemoria(RELOJ);
        metricas.encolado(TipoNotificacion.NUEVA_OPORTUNIDAD);
        metricas.fallo("VINCULO_REVOCADO");
        metricas.pendientes(new PendientesPush(1, 1, AHORA));

        String linea = metricas.resumen().describir();

        assertTrue(linea.contains("NUEVA_OPORTUNIDAD"));
        assertTrue(linea.contains("VINCULO_REVOCADO"));
        assertFalse(linea.contains("token"), "la línea operativa nunca transporta tokens");
        assertFalse(linea.contains("instalacion"), "tampoco identificadores de instalación");
        // Los contadores se ordenan para que el resumen sea comparable entre ciclos.
        List<String> claves = List.copyOf(metricas.resumen().encolados().keySet());
        assertEquals(List.of("NUEVA_OPORTUNIDAD"), claves);
    }
}
