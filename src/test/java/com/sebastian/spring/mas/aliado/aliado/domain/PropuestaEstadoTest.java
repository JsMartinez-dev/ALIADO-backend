package com.sebastian.spring.mas.aliado.aliado.domain;

import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.exception.PropuestaNoActivaException;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.EstadoPropuesta;
import com.sebastian.spring.mas.aliado.aliado.modules.propuestas.domain.model.Propuesta;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Máquina de estados de Propuesta (SPEC-07). */
class PropuestaEstadoTest {

    private Propuesta propuesta(EstadoPropuesta estado) {
        return new Propuesta(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Lunes 8-12",
                "Puedo hacerlo", estado, OffsetDateTime.now(), OffsetDateTime.now(), 0);
    }

    @Test
    void retirarPasaARetirada() {
        assertEquals(EstadoPropuesta.RETIRADA,
                propuesta(EstadoPropuesta.ACTIVA).retirar(OffsetDateTime.now()).estado());
    }

    @Test
    void aceptarPasaAAceptada() {
        assertEquals(EstadoPropuesta.ACEPTADA,
                propuesta(EstadoPropuesta.ACTIVA).aceptar(OffsetDateTime.now()).estado());
    }

    @Test
    void cerrarPasaACerrada() {
        assertEquals(EstadoPropuesta.CERRADA,
                propuesta(EstadoPropuesta.ACTIVA).cerrar(OffsetDateTime.now()).estado());
    }

    @Test
    void retirarPropuestaRetiradaEsInvalido() {
        assertThrows(PropuestaNoActivaException.class,
                () -> propuesta(EstadoPropuesta.RETIRADA).retirar(OffsetDateTime.now()));
    }

    @Test
    void editarNoCambiaEstado() {
        var propuesta = propuesta(EstadoPropuesta.ACTIVA);
        assertEquals(EstadoPropuesta.ACTIVA, propuesta.editada("Martes", null, OffsetDateTime.now()).estado());
    }
}
