package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResultadoLotePush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ProcesadorTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.ReclamacionTrabajoPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.scheduler.EscuchaTrabajoPush;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-06: con la propiedad activada, la composición arranca el coordinador contra
 * el puerto de reclamación y la escucha dedicada. Se sustituyen ambos por dobles para no
 * abrir PostgreSQL ni depender de F09-PUSH-07.
 */
@Timeout(30)
@SpringBootTest(properties = {
        "aliado.push.worker-habilitado=true",
        "aliado.push.escucha.url=jdbc:postgresql://127.0.0.1:1/no-se-abre"
})
class WorkerPushConfigTest {

    @Autowired private EscuchaFalsa escucha;
    @Autowired private ReclamacionFalsa reclamacion;

    @Test
    void laPropiedadActivaComponeYArrancaElCoordinador() throws Exception {
        assertTrue(reclamacion.llamadas.await(5, TimeUnit.SECONDS));
        assertTrue(escucha.aperturas.get() >= 1, "el coordinador debe suscribir la escucha");
    }

    @TestConfiguration
    static class Sustituciones {
        @Bean @Primary EscuchaFalsa escuchaFalsa() {
            return new EscuchaFalsa();
        }

        @Bean @Primary ReclamacionFalsa reclamacionFalsa() {
            return new ReclamacionFalsa();
        }

        /** F09-PUSH-07: la composición exige el procesador que implementará F09-PUSH-08. */
        @Bean @Primary ProcesadorFalso procesadorFalso() {
            return new ProcesadorFalso();
        }
    }

    static final class ProcesadorFalso implements ProcesadorTrabajoPushPort {
        @Override
        public void procesar(ReclamacionPush reclamacion) {
            // Doble: el despacho real llega en F09-PUSH-08.
        }
    }

    static final class ReclamacionFalsa implements ReclamacionTrabajoPushPort {
        final CountDownLatch llamadas = new CountDownLatch(1);

        @Override
        public ResultadoLotePush procesarLoteElegible() {
            llamadas.countDown();
            return new ResultadoLotePush(0, false);
        }

        @Override
        public Optional<Instant> proximoPlazo() {
            return Optional.empty();
        }
    }

    static final class EscuchaFalsa implements EscuchaTrabajoPush {
        final AtomicInteger aperturas = new AtomicInteger();
        private volatile Consumer<Exception> alPerder;
        private final CountDownLatch cierre = new CountDownLatch(1);
        private volatile boolean activa;

        @Override
        public void abrir() {
            aperturas.incrementAndGet();
            activa = true;
        }

        @Override
        public void escuchar(Runnable alRecibirSenal, Consumer<Exception> alPerderConexion) {
            alPerder = alPerderConexion;
            try {
                cierre.await();
            } catch (InterruptedException interrumpido) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean estaActiva() {
            return activa;
        }

        @Override
        public void close() {
            activa = false;
            if (alPerder != null) {
                alPerder = null;
            }
            cierre.countDown();
        }
    }
}
