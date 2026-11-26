package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IteracionTrabajoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MotivoDespertarTrabajo;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoIteracionTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.ProcesarTrabajoPushService;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-06: la iteración del worker devuelve si resta trabajo o el próximo plazo
 * persistido. La reclamación real llega en F09-PUSH-07; aquí se usa un doble.
 */
class ProcesarTrabajoPushServiceTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void unLoteConTrabajoRestanteOrdenaDrenarDeInmediatoSinConsultarPlazos() {
        ReclamacionFalsa reclamacion = new ReclamacionFalsa(new ResultadoLotePush(5, true),
                Optional.of(AHORA.plusSeconds(30)));
        ProcesarTrabajoPushService servicio = new ProcesarTrabajoPushService(reclamacion);

        ResultadoIteracionTrabajoPush resultado = servicio.ejecutar(comando(MotivoDespertarTrabajo.SENAL));

        assertTrue(resultado.quedanElegibles());
        assertTrue(resultado.proximoPlazo().isEmpty(), "con lote lleno no se arma temporizador");
        assertEquals(0, reclamacion.consultasPlazo.get(), "el plazo se consulta solo al agotar el lote");
    }

    @Test
    void sinTrabajoRestanteDevuelveElProximoPlazoPersistido() {
        Instant plazo = AHORA.plusSeconds(45);
        ProcesarTrabajoPushService servicio = new ProcesarTrabajoPushService(
                new ReclamacionFalsa(new ResultadoLotePush(1, false), Optional.of(plazo)));

        ResultadoIteracionTrabajoPush resultado = servicio.ejecutar(comando(MotivoDespertarTrabajo.ARRANQUE));

        assertEquals(false, resultado.quedanElegibles());
        assertEquals(Optional.of(plazo), resultado.proximoPlazo());
    }

    @Test
    void conColaVaciaYSinPlazosQuedaEnReposo() {
        ProcesarTrabajoPushService servicio = new ProcesarTrabajoPushService(
                new ReclamacionFalsa(new ResultadoLotePush(0, false), Optional.empty()));

        ResultadoIteracionTrabajoPush resultado = servicio.ejecutar(comando(MotivoDespertarTrabajo.TEMPORIZADOR));

        assertEquals(false, resultado.quedanElegibles());
        assertTrue(resultado.proximoPlazo().isEmpty());
    }

    private static IteracionTrabajoPushCommand comando(MotivoDespertarTrabajo motivo) {
        return new IteracionTrabajoPushCommand(motivo);
    }

    private static final class ReclamacionFalsa implements ReclamacionTrabajoPushPort {
        private final ResultadoLotePush lote;
        private final Optional<Instant> plazo;
        final AtomicInteger consultasPlazo = new AtomicInteger();

        ReclamacionFalsa(ResultadoLotePush lote, Optional<Instant> plazo) {
            this.lote = lote;
            this.plazo = plazo;
        }

        @Override
        public ResultadoLotePush procesarLoteElegible() {
            return lote;
        }

        @Override
        public Optional<Instant> proximoPlazo() {
            consultasPlazo.incrementAndGet();
            return plazo;
        }
    }
}
