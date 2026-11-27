package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.RevalidacionOportunidadPushAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.EvaluarCompatibilidadPrestadorQuery;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-08: el puerto de notificaciones delega en el caso de uso público de
 * oportunidades, sin acoplarse a su persistencia.
 */
class RevalidacionOportunidadPushAdapterTest {

    @Test
    void delegaLaParejaSolicitudPrestadorEnElCasoDeUsoDeOportunidades() {
        UUID solicitud = UUID.randomUUID();
        UUID prestador = UUID.randomUUID();
        AtomicReference<EvaluarCompatibilidadPrestadorQuery> consulta = new AtomicReference<>();

        RevalidacionOportunidadPushAdapter adaptador = new RevalidacionOportunidadPushAdapter(query -> {
            consulta.set(query);
            return true;
        });

        assertTrue(adaptador.sigueVigente(solicitud, prestador));
        assertEquals(solicitud, consulta.get().solicitudId());
        assertEquals(prestador, consulta.get().prestadorId());
    }

    @Test
    void propagaLaRespuestaNegativaDelCasoDeUso() {
        RevalidacionOportunidadPushAdapter adaptador = new RevalidacionOportunidadPushAdapter(
                query -> false);

        assertFalse(adaptador.sigueVigente(UUID.randomUUID(), UUID.randomUUID()));
    }
}
