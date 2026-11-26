package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.ReclamacionTrabajoPushAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-07: el adaptador de reclamación abre la transacción corta, reclama el lote y
 * solo después procesa cada elemento fuera de esa transacción.
 */
class ReclamacionTrabajoPushAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-06-01T00:00:00Z");
    private static final Duration VIGENCIA = Duration.ofSeconds(30);

    @Test
    void procesaCadaReclamacionDespuesDeConfirmarLaReclamacion() {
        ReclamacionPush preparacion = ReclamacionPush.preparacion(UUID.randomUUID(),
                UUID.randomUUID(), AHORA.plusSeconds(30));
        ReclamacionPush envio = ReclamacionPush.envio(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 1, UUID.randomUUID(), AHORA.plusSeconds(30));
        RepositorioFalso repositorio = new RepositorioFalso(
                new LoteReclamadoPush(List.of(preparacion, envio), false));
        ProcesadorFalso procesador = new ProcesadorFalso(repositorio);

        ResultadoLotePush resultado = adaptador(repositorio, procesador, 8).procesarLoteElegible();

        assertEquals(2, resultado.procesados());
        assertFalse(resultado.quedanElegibles());
        assertEquals(List.of(preparacion, envio), procesador.procesadas);
        assertEquals(1, repositorio.reclamaciones, "la reclamación se pide una sola vez");
        assertEquals(8, repositorio.maximo);
        assertEquals(VIGENCIA, repositorio.vencimiento);
    }

    @Test
    void loteVacioNoInvocaAlProcesadorNiPideDrenar() {
        RepositorioFalso repositorio = new RepositorioFalso(LoteReclamadoPush.vacio());
        ProcesadorFalso procesador = new ProcesadorFalso(repositorio);

        ResultadoLotePush resultado = adaptador(repositorio, procesador, 8).procesarLoteElegible();

        assertEquals(0, resultado.procesados());
        assertFalse(resultado.quedanElegibles());
        assertTrue(procesador.procesadas.isEmpty());
    }

    @Test
    void unLoteSaturadoOrdenaDrenarDeInmediato() {
        List<ReclamacionPush> reclamaciones = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            reclamaciones.add(ReclamacionPush.preparacion(UUID.randomUUID(), UUID.randomUUID(),
                    AHORA.plusSeconds(30)));
        }
        RepositorioFalso repositorio = new RepositorioFalso(
                new LoteReclamadoPush(reclamaciones, true));

        ResultadoLotePush resultado = adaptador(repositorio, new ProcesadorFalso(repositorio), 2)
                .procesarLoteElegible();

        assertEquals(2, resultado.procesados());
        assertTrue(resultado.quedanElegibles(), "el lote lleno deja trabajo pendiente");
    }

    @Test
    void elProximoPlazoLoResuelveElRepositorio() {
        Instant plazo = AHORA.plusSeconds(12);
        RepositorioFalso repositorio = new RepositorioFalso(LoteReclamadoPush.vacio());
        repositorio.plazo = Optional.of(plazo);

        assertEquals(Optional.of(plazo),
                adaptador(repositorio, new ProcesadorFalso(repositorio), 8).proximoPlazo());
    }

    private static ReclamacionTrabajoPushAdapter adaptador(ReclamacionTrabajoPushRepository repositorio,
                                                           ProcesadorTrabajoPushPort procesador,
                                                           int maximo) {
        return new ReclamacionTrabajoPushAdapter(repositorio, procesador,
                new TransactionTemplate(GESTOR_SIN_TRANSACCION_REAL), maximo, VIGENCIA);
    }

    /** Gestor mínimo: el doble de repositorio no necesita una transacción real. */
    private static final PlatformTransactionManager GESTOR_SIN_TRANSACCION_REAL =
            new PlatformTransactionManager() {
                @Override
                public TransactionStatus getTransaction(TransactionDefinition definicion) {
                    return new SimpleTransactionStatus();
                }

                @Override
                public void commit(TransactionStatus estado) {
                }

                @Override
                public void rollback(TransactionStatus estado) {
                }
            };

    private static final class RepositorioFalso implements ReclamacionTrabajoPushRepository {
        private final LoteReclamadoPush lote;
        final List<String> eventos = new CopyOnWriteArrayList<>();
        Optional<Instant> plazo = Optional.empty();
        int maximo;
        Duration vencimiento;
        int reclamaciones;
        volatile boolean reclamando;

        RepositorioFalso(LoteReclamadoPush lote) {
            this.lote = lote;
        }

        @Override
        public LoteReclamadoPush reclamar(int maximo, Duration vencimiento) {
            this.maximo = maximo;
            this.vencimiento = vencimiento;
            reclamaciones++;
            reclamando = true;
            try {
                eventos.add("reclamar");
                return lote;
            } finally {
                reclamando = false;
            }
        }

        @Override
        public boolean confirmarPreparacion(UUID trabajoId, UUID reclamacionId) {
            return false;
        }

        @Override
        public boolean guardarResultadoEntrega(ResultadoEntregaPush resultado) {
            return false;
        }

        @Override
        public Optional<Instant> proximoPlazo() {
            return plazo;
        }
    }

    private static final class ProcesadorFalso implements ProcesadorTrabajoPushPort {
        private final RepositorioFalso repositorio;
        final List<ReclamacionPush> procesadas = new CopyOnWriteArrayList<>();

        ProcesadorFalso(RepositorioFalso repositorio) {
            this.repositorio = repositorio;
        }

        @Override
        public void procesar(ReclamacionPush reclamacion) {
            // La transacción de reclamación ya se confirmó antes de procesar.
            assertFalse(repositorio.reclamando,
                    "el procesamiento no puede ocurrir dentro de la transacción de reclamación");
            procesadas.add(reclamacion);
        }
    }
}
