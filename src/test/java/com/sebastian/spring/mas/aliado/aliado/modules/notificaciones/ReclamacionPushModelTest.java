package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * F09-PUSH-07: invariantes de las reclamaciones y de los resultados de entrega, alineadas
 * con las restricciones de V14.
 */
class ReclamacionPushModelTest {

    private static final Instant VENCE = Instant.parse("2026-06-01T00:00:30Z");

    @Test
    void unaReclamacionDePreparacionNoIdentificaDispositivo() {
        ReclamacionPush reclamacion = ReclamacionPush.preparacion(UUID.randomUUID(), UUID.randomUUID(), VENCE);

        assertEquals(TipoReclamacionPush.PREPARACION, reclamacion.tipo());
        assertNull(reclamacion.entregaId());
        assertNull(reclamacion.instalacionId());

        assertThrows(IllegalArgumentException.class, () -> new ReclamacionPush(
                TipoReclamacionPush.PREPARACION, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), 1L, UUID.randomUUID(), VENCE));
    }

    @Test
    void unaReclamacionDeEnvioExigeEntregaInstalacionYGeneracion() {
        assertThrows(IllegalArgumentException.class, () -> new ReclamacionPush(
                TipoReclamacionPush.ENVIO, UUID.randomUUID(), null, null, null,
                UUID.randomUUID(), VENCE));
        assertThrows(IllegalArgumentException.class, () -> ReclamacionPush.envio(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 0, UUID.randomUUID(), VENCE));
    }

    @Test
    void unReintentoExigeProximoIntentoYUnTerminalLoProhibe() {
        assertThrows(IllegalArgumentException.class, () -> resultado(EstadoEntregaPush.PENDIENTE, null, null));
        assertThrows(IllegalArgumentException.class, () -> resultado(
                EstadoEntregaPush.ACEPTADA, VENCE.plusSeconds(60), "mensaje"));
    }

    @Test
    void soloLaAceptacionLlevaIdentificadorDeMensaje() {
        assertThrows(IllegalArgumentException.class, () -> resultado(EstadoEntregaPush.ACEPTADA, null, null));
        assertThrows(IllegalArgumentException.class, () -> resultado(
                EstadoEntregaPush.FALLIDA_DEFINITIVA, null, "mensaje"));
    }

    private static ResultadoEntregaPush resultado(EstadoEntregaPush estado, Instant proximo,
                                                  String idMensaje) {
        return new ResultadoEntregaPush(UUID.randomUUID(), UUID.randomUUID(), 1, estado, 1,
                proximo, idMensaje, null);
    }
}
