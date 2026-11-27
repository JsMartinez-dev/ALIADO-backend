package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-BANDEJA-01 sobre PostgreSQL real: Flyway aplica V15 sobre el esquema del piloto y
 * Hibernate valida el mapeo de {@code leida_en}. Comprueba que la fecha de primera lectura
 * se carga, sobrevive a una actualización del flujo de entrega y que un aviso nuevo nace
 * pendiente.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(120)
@SpringBootTest
class LecturaBandejaPersistenciaPostgresTest {

    private static final OffsetDateTime GENERADA = OffsetDateTime.of(2026, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime LEIDA = OffsetDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("ALIADO_PG_URL"));
        registry.add("spring.datasource.username", () -> leer("ALIADO_PG_USERNAME", "aliado"));
        registry.add("spring.datasource.password", () -> leer("ALIADO_PG_PASSWORD", "aliado"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private NotificacionRepository notificaciones;

    @BeforeEach
    void limpiar() {
        jdbc.update("DELETE FROM entregas_push");
        jdbc.update("DELETE FROM trabajos_push");
        jdbc.update("DELETE FROM vinculos_push");
        jdbc.update("DELETE FROM instalaciones_push");
        jdbc.update("DELETE FROM notificaciones");
    }

    @Test
    void laFechaDeLecturaSeCargaYSobreviveAUnaActualizacionDeEntrega() {
        UUID usuario = sembrarUsuario();
        UUID id = sembrarAvisoEntregado(usuario, LEIDA);

        Notificacion cargada = notificaciones.porId(id).orElseThrow();
        assertEquals(LEIDA.toInstant(), cargada.leidaEn().toInstant());

        // Simula la escritura del flujo de entrega: cambia estado e intentos, no la lectura.
        notificaciones.guardar(new Notificacion(cargada.id(), cargada.destinatarioId(), cargada.tipo(),
                cargada.aggregateId(), cargada.eventVersion(), cargada.detalle(), cargada.canal(),
                EstadoEntrega.ENTREGADA, cargada.intentos() + 1, cargada.fechaGeneracion(),
                cargada.fechaEntrega(), cargada.leidaEn()));

        assertEquals(LEIDA.toInstant(), leidaEn(id).toInstant());
        assertEquals(2, jdbc.queryForObject(
                "SELECT intentos FROM notificaciones WHERE id = ?", Integer.class, id));
    }

    @Test
    void unAvisoNuevoSeCreaPendienteConLeidaEnNula() {
        UUID usuario = sembrarUsuario();
        UUID id = UUID.randomUUID();
        Notificacion nueva = new Notificacion(id, usuario, TipoNotificacion.NUEVA_OPORTUNIDAD,
                UUID.randomUUID(), 1, "detalle", Canal.PUSH, EstadoEntrega.PENDIENTE, 0,
                GENERADA, null, null);

        assertTrue(notificaciones.insertarSiNoExiste(nueva));

        assertNull(leidaEn(id));
    }

    private OffsetDateTime leidaEn(UUID id) {
        return jdbc.queryForObject("SELECT leida_en FROM notificaciones WHERE id = ?",
                (rs, fila) -> rs.getObject("leida_en", OffsetDateTime.class), id);
    }

    private UUID sembrarUsuario() {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO usuarios (id, email, password_hash, rol, estado_cuenta, fecha_creacion)
                VALUES (?, ?, 'x', 'PRESTADOR', 'ACTIVA', ?)
                """, id, "prestador-" + id + "@test.local", OffsetDateTime.now());
        return id;
    }

    private UUID sembrarAvisoEntregado(UUID destinatario, OffsetDateTime leidaEn) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version,
                                            detalle, canal, estado_entrega, intentos, fecha_generacion,
                                            fecha_entrega, leida_en)
                VALUES (?, ?, 'NUEVA_OPORTUNIDAD', ?, 1, 'detalle', 'PUSH', 'ENTREGADA', 1, ?, ?, ?)
                """, id, destinatario, UUID.randomUUID(), GENERADA, GENERADA.plusSeconds(1), leidaEn);
        return id;
    }

    private static String leer(String variable, String predeterminado) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? predeterminado : valor;
    }
}
