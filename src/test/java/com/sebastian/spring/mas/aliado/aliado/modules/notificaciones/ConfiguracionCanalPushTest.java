package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ConfiguracionCanalPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-11: activación por entorno y tipo. El piloto solo habilita
 * {@code NUEVA_OPORTUNIDAD}; el resto de tipos sigue en la bandeja sin push.
 */
class ConfiguracionCanalPushTest {

    @Test
    void elPilotoHabilitaSoloNuevaOportunidad() {
        ConfiguracionCanalPush configuracion = ConfiguracionCanalPush.desde(true, "NUEVA_OPORTUNIDAD");

        assertTrue(configuracion.habilitadoPara(TipoNotificacion.NUEVA_OPORTUNIDAD));
        assertFalse(configuracion.habilitadoPara(TipoNotificacion.PROPUESTA_RECIBIDA));
        assertFalse(configuracion.habilitadoPara(TipoNotificacion.ACEPTACION_PROPUESTA));
        assertFalse(configuracion.habilitadoPara(TipoNotificacion.CAMBIO_ESTADO_SERVICIO));
        assertFalse(configuracion.habilitadoPara(TipoNotificacion.AVISO_SEGURIDAD_CUENTA));
    }

    @Test
    void elApagadoGlobalDetieneElCanalAunqueElTipoSigaListado() {
        ConfiguracionCanalPush configuracion = ConfiguracionCanalPush.desde(false, "NUEVA_OPORTUNIDAD");

        assertFalse(configuracion.habilitadoPara(TipoNotificacion.NUEVA_OPORTUNIDAD));
    }

    @Test
    void unaListaVaciaNoHabilitaNingunTipo() {
        ConfiguracionCanalPush configuracion = ConfiguracionCanalPush.desde(true, " , ");

        assertEquals(Set.of(), configuracion.tiposHabilitados());
        assertFalse(configuracion.habilitadoPara(TipoNotificacion.NUEVA_OPORTUNIDAD));
    }

    @Test
    void unTipoDesconocidoFallaEnLaComposicion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ConfiguracionCanalPush.desde(true, "NUEVA_OPORTUNIDAD, NUEVA_OPORTUNIDDA"));

        assertTrue(error.getMessage().contains("NUEVA_OPORTUNIDDA"),
                "la errata debe verse en el mensaje para no apagar el canal en silencio");
    }
}
