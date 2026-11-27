package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Credenciales y aplicación de Firebase para el piloto push (F09-PUSH-09).
 *
 * <p>Las credenciales se inyectan desde el gestor de secretos del entorno: o bien el JSON
 * completo de la cuenta de servicio en {@code aliado.push.firebase.credenciales-json}
 * (por ejemplo, montado desde el secreto), o bien la ruta estándar de
 * {@code GOOGLE_APPLICATION_CREDENTIALS} que resuelve la credencial por defecto de Google.
 * El proyecto se separa por entorno con {@code aliado.push.firebase.proyecto-id}. Ningún
 * valor de credenciales se registra ni se expone en mensajes de error.</p>
 *
 * <p>Solo se activa con {@code aliado.push.proveedor=firebase}; el piloto local y las
 * pruebas usan el adaptador falso. Al seleccionar Firebase, una configuración incompleta
 * falla al arrancar en lugar de dejar el worker sin proveedor.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "aliado.push.proveedor", havingValue = "firebase")
public class FirebasePushConfig {

    private static final String NOMBRE_APP = "aliado-push";

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseAppPush(
            @Value("${aliado.push.firebase.proyecto-id:}") String proyectoId,
            @Value("${aliado.push.firebase.credenciales-json:}") String credencialesJson) {
        return FirebaseApp.initializeApp(opciones(proyectoId, credencialesJson), NOMBRE_APP);
    }

    static FirebaseOptions opciones(String proyectoId, String credencialesJson) {
        if (proyectoId == null || proyectoId.isBlank()) {
            throw new IllegalStateException("Falta aliado.push.firebase.proyecto-id"
                    + " (ALIADO_FCM_PROYECTO_ID) para configurar FCM");
        }
        return FirebaseOptions.builder()
                .setProjectId(proyectoId.trim())
                .setCredentials(credenciales(credencialesJson))
                .build();
    }

    private static GoogleCredentials credenciales(String credencialesJson) {
        try {
            if (credencialesJson != null && !credencialesJson.isBlank()) {
                return GoogleCredentials.fromStream(new ByteArrayInputStream(
                        credencialesJson.getBytes(StandardCharsets.UTF_8)));
            }
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException | RuntimeException noDisponibles) {
            // Sin causa encadenada: los mensajes del parser pueden reproducir fragmentos
            // del secreto. El texto solo nombra las propiedades que deben configurarse.
            throw new IllegalStateException("No se pudieron cargar las credenciales de Firebase."
                    + " Configura aliado.push.firebase.credenciales-json"
                    + " (ALIADO_FCM_CREDENCIALES_JSON) o GOOGLE_APPLICATION_CREDENTIALS");
        }
    }
}
