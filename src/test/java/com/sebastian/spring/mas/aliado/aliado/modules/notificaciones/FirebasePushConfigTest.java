package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config.FirebasePushConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-09: la configuración de Firebase exige el proyecto por entorno y no reproduce
 * el contenido de las credenciales en los mensajes de error.
 */
class FirebasePushConfigTest {

    private final FirebasePushConfig configuracion = new FirebasePushConfig();

    @Test
    void exigeElProyectoDelEntornoAntesDeLeerCredenciales() {
        IllegalStateException fallo = assertThrows(IllegalStateException.class,
                () -> configuracion.firebaseAppPush("", "{\"type\":\"service_account\"}"));

        assertTrue(fallo.getMessage().contains("aliado.push.firebase.proyecto-id"));
        assertFalse(fallo.getMessage().contains("service_account"));
        assertEquals(null, fallo.getCause());
    }

    @Test
    void unasCredencialesInvalidasFallanSinReproducirSuContenido() {
        String secreto = "{\"clave\":\"valor-que-no-debe-aparecer\"";
        IllegalStateException fallo = assertThrows(IllegalStateException.class,
                () -> configuracion.firebaseAppPush("aliado-piloto", secreto));

        assertTrue(fallo.getMessage().contains("credenciales de Firebase"));
        assertFalse(fallo.getMessage().contains("valor-que-no-debe-aparecer"));
        assertEquals(null, fallo.getCause(),
                "no se encadena el error del parser porque puede reproducir el secreto");
    }
}
