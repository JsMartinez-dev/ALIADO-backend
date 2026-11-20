package com.sebastian.spring.mas.aliado.aliado.domain;

import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.exception.TransicionServicioInvalidaException;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Máquina de estados de ServicioContratado (SPEC-08): secuencia lineal estricta. */
class ServicioEstadoTest {

    private ServicioContratado servicio(EstadoServicio estado) {
        return new ServicioContratado(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), estado, true, OffsetDateTime.now(), 0,
                List.of(), List.of());
    }

    @Test
    void secuenciaLineal() {
        var servicio = servicio(EstadoServicio.CONTRATADO)
                .transitarA(EstadoServicio.EN_EJECUCION, UUID.randomUUID(), OffsetDateTime.now())
                .transitarA(EstadoServicio.TERMINADO, UUID.randomUUID(), OffsetDateTime.now())
                .transitarA(EstadoServicio.FINALIZADO, UUID.randomUUID(), OffsetDateTime.now());
        assertEquals(EstadoServicio.FINALIZADO, servicio.estado());
        assertEquals(3, servicio.historial().size());
    }

    @Test
    void terminadoExigeEnEjecucion() {
        assertThrows(TransicionServicioInvalidaException.class, () -> servicio(EstadoServicio.CONTRATADO)
                .transitarA(EstadoServicio.TERMINADO, UUID.randomUUID(), OffsetDateTime.now()));
    }

    @Test
    void finalizadoEsTerminal() {
        assertThrows(TransicionServicioInvalidaException.class, () -> servicio(EstadoServicio.FINALIZADO)
                .transitarA(EstadoServicio.CANCELADO, UUID.randomUUID(), OffsetDateTime.now()));
    }

    @Test
    void canceladoEsTerminal() {
        assertThrows(TransicionServicioInvalidaException.class, () -> servicio(EstadoServicio.CANCELADO)
                .transitarA(EstadoServicio.EN_EJECUCION, UUID.randomUUID(), OffsetDateTime.now()));
    }

    @Test
    void cancelarPermitidoAntesDeFinalizar() {
        assertEquals(EstadoServicio.CANCELADO, servicio(EstadoServicio.TERMINADO)
                .transitarA(EstadoServicio.CANCELADO, UUID.randomUUID(), OffsetDateTime.now()).estado());
    }
}
