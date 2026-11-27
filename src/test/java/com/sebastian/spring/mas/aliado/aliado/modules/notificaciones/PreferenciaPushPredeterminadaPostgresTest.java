package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarAvisoOportunidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarAvisoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.dto.PreferenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.usecase.ListarPreferenciasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-05 sobre PostgreSQL real con el adaptador de preferencias real: sin elección
 * persistida, {@code NUEVA_OPORTUNIDAD/PUSH} vale {@code false} (F09-PUSH-01, D-08), así
 * que publicar conserva el aviso de bandeja y no encola trabajo ni emite señal. Con fila
 * {@code habilitada=true} se encola un trabajo y se emite una señal; con {@code false} no.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable. La suite
 * estándar (H2) queda intacta.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(90)
@SpringBootTest
class PreferenciaPushPredeterminadaPostgresTest {

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("ALIADO_PG_URL"));
        registry.add("spring.datasource.username", () -> leer("ALIADO_PG_USERNAME", "aliado"));
        registry.add("spring.datasource.password", () -> leer("ALIADO_PG_PASSWORD", "aliado"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private CrearSolicitudUseCase crear;
    @Autowired private RegistrarAvisoOportunidadUseCase registrarAviso;
    @Autowired private ListarPreferenciasUseCase listarPreferencias;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private CompatibilidadFalsa compatibilidad;

    private Datos datos;

    @BeforeEach
    void sembrar() {
        datos = sembrarDatos();
        compatibilidad.prestadores = List.of(datos.prestador());
    }

    @AfterEach
    void quitarFila() {
        jdbc.update("DELETE FROM preferencias_notificacion WHERE usuario_id = ?", datos.prestador());
    }

    @Test
    void sinFilaElAvisoQuedaEnLaBandejaSinTrabajoNiSenalYElListadoDevuelveApagado() throws Exception {
        assertEquals(0, contarElecciones(datos.prestador()), "el escenario parte sin elección persistida");

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            crear.ejecutar(comandoPublicado());

            assertEquals(1, contarAvisos(datos.prestador()), "la bandeja conserva el aviso");
            assertEquals(0, contarTrabajos(datos.prestador()), "sin elección no se encola push");
            assertEquals(0, escucha.esperar(1_500).length, "sin trabajo no hay señal");
        }

        var piloto = preferencia("NUEVA_OPORTUNIDAD", "PUSH");
        assertFalse(piloto.habilitada(), "el listado devuelve el mismo default que el encolado");
        assertTrue(piloto.configurable());
    }

    @Test
    void conFilaHabilitadaSeEncolaUnTrabajoYSeEmiteUnaSenal() throws Exception {
        guardarEleccion(true);

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            crear.ejecutar(comandoPublicado());

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
            assertEquals(1, escucha.esperar(5_000).length, "el commit entrega la señal");
        }

        assertTrue(preferencia("NUEVA_OPORTUNIDAD", "PUSH").habilitada());
    }

    @Test
    void conFilaApagadaElAvisoSeConservaSinTrabajoNiSenal() throws Exception {
        guardarEleccion(false);

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            crear.ejecutar(comandoPublicado());

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(0, contarTrabajos(datos.prestador()));
            assertEquals(0, escucha.esperar(1_500).length);
        }

        assertFalse(preferencia("NUEVA_OPORTUNIDAD", "PUSH").habilitada());
    }

    @Test
    void elRegistroDirectoSinFilaTampocoEncolaTrabajo() throws Exception {
        // Sin fila, un registro directo del caso de uso tampoco debe programar push.
        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            jdbc.update("""
                    INSERT INTO solicitudes (id, cliente_id, categoria_id, zona_id, descripcion,
                                             urgencia, estado, fecha_creacion, fecha_actualizacion, version)
                    VALUES (?, ?, ?, ?, 'directa', 'MEDIA', 'PUBLICADA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                    """, datos.solicitud(), datos.cliente(), datos.categoria(), datos.zona());
            registrarAviso.ejecutar(new RegistrarAvisoOportunidadCommand(datos.solicitud(),
                    datos.prestador(), 1, "detalle"));

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(0, contarTrabajos(datos.prestador()));
            assertEquals(0, escucha.esperar(1_500).length);
        }
    }

    private CrearSolicitudCommand comandoPublicado() {
        return new CrearSolicitudCommand(datos.cliente(), datos.categoria(), "Descripción",
                datos.zona(), "MEDIA", "PUBLICADA");
    }

    private void guardarEleccion(boolean habilitada) {
        jdbc.update("""
                INSERT INTO preferencias_notificacion (usuario_id, tipo_aviso, canal, habilitada, configurable)
                VALUES (?, 'NUEVA_OPORTUNIDAD', 'PUSH', ?, TRUE)
                """, datos.prestador(), habilitada);
    }

    private PreferenciaResult preferencia(String tipoAviso, String canal) {
        return listarPreferencias.ejecutar(datos.prestador()).stream()
                .filter(p -> p.tipoAviso().equals(tipoAviso) && p.canal().equals(canal))
                .findFirst().orElseThrow();
    }

    private Datos sembrarDatos() {
        UUID cliente = UUID.randomUUID();
        UUID prestador = UUID.randomUUID();
        UUID categoria = UUID.randomUUID();
        UUID zona = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        jdbc.update("INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion) "
                + "VALUES (?, ?, ?, 'CLIENTE', 'ACTIVA', ?)", cliente, "cliente-" + cliente + "@test.local", "x", ahora);
        jdbc.update("INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion) "
                + "VALUES (?, ?, ?, 'PRESTADOR', 'ACTIVA', ?)", prestador, "prestador-" + prestador + "@test.local", "x", ahora);
        jdbc.update("INSERT INTO categorias_servicio (id, nombre, estado) VALUES (?, ?, 'ACTIVO')",
                categoria, "Categoría " + categoria);
        jdbc.update("INSERT INTO zonas (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", zona, "Zona " + zona);
        return new Datos(cliente, prestador, categoria, zona, UUID.randomUUID());
    }

    private int contarElecciones(UUID prestador) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM preferencias_notificacion WHERE usuario_id = ?", Integer.class, prestador);
        return total == null ? 0 : total;
    }

    private int contarAvisos(UUID prestador) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificaciones WHERE destinatario_id = ? AND tipo = 'NUEVA_OPORTUNIDAD'",
                Integer.class, prestador);
        return total == null ? 0 : total;
    }

    private int contarTrabajos(UUID prestador) {
        Integer total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM trabajos_push t
                JOIN notificaciones n ON n.id = t.notificacion_id
                WHERE n.destinatario_id = ? AND t.canal = 'PUSH'
                """, Integer.class, prestador);
        return total == null ? 0 : total;
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    private record Datos(UUID cliente, UUID prestador, UUID categoria, UUID zona, UUID solicitud) {}

    @TestConfiguration
    static class Sustituciones {
        @Bean @Primary CompatibilidadFalsa compatibilidadFalsa() { return new CompatibilidadFalsa(); }
    }

    static final class CompatibilidadFalsa implements CompatibilidadPort {
        volatile List<UUID> prestadores = List.of();

        @Override public boolean esCompatible(UUID solicitudId, UUID prestadorId) {
            return prestadores.contains(prestadorId);
        }

        @Override public List<UUID> prestadoresCompatibles(UUID solicitudId) { return prestadores; }
    }
}
