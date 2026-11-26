package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarAvisoOportunidadCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarAvisoOportunidadUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.RegistroOportunidadNotificacionAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.SenalTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.RegistroNotificacionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.GuardarBorradorCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.PublicarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.GuardarBorradorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.PublicarSolicitudUseCase;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-05 sobre PostgreSQL real: las dos rutas de publicación persisten aviso y
 * trabajo en la transacción de negocio, el rollback no deja filas ni señal, el commit
 * despierta a un listener separado y dos consumos concurrentes no duplican ni abortan
 * la transacción.
 *
 * <p>Se ejecuta solo con {@code ALIADO_PG_URL} definido y una base desechable. La suite
 * estándar (H2) queda intacta.</p>
 */
@EnabledIfEnvironmentVariable(named = "ALIADO_PG_URL", matches = ".+")
@Timeout(90)
@SpringBootTest
class PublicacionPushPostgresTest {

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
    @Autowired private GuardarBorradorUseCase guardarBorrador;
    @Autowired private PublicarSolicitudUseCase publicar;
    @Autowired private RegistrarAvisoOportunidadUseCase registrarAviso;
    @Autowired private PublicadorEventos publicador;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;

    @Autowired private CompatibilidadFalsa compatibilidad;
    @Autowired private PreferenciasFalsas preferencias;
    @Autowired private CanalBloqueante canal;
    @Autowired private RegistroConFallo registro;

    @BeforeEach
    void reiniciarSustituciones() {
        preferencias.habilitado = true;
        canal.entregas.set(0);
        registro.fallar = false;
        compatibilidad.prestadores = List.of();
    }

    @Test
    void publicacionDirectaEncolaAvisoYTrabajoYElListenerDespiertaSoloAlConfirmar() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            long inicio = System.nanoTime();
            crear.ejecutar(new CrearSolicitudCommand(datos.cliente(), datos.categoria(),
                    "Descripción", datos.zona(), "MEDIA", "PUBLICADA"));
            long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
            var trabajo = jdbc.queryForMap("""
                    SELECT t.estado, t.canal, t.expira_en > t.encolado_en AS vigente
                    FROM trabajos_push t JOIN notificaciones n ON n.id = t.notificacion_id
                    WHERE n.destinatario_id = ?
                    """, datos.prestador());
            assertEquals("PENDIENTE_PREPARACION", trabajo.get("estado"));
            assertEquals("PUSH", trabajo.get("canal"));
            assertEquals(Boolean.TRUE, trabajo.get("vigente"));
            assertEquals(0, canal.entregas.get(), "publicar no debe invocar al canal FCM");
            assertTrue(transcurridoMs < 2_000, "publicar no debe esperar la entrega externa");

            PGNotification[] senales = escucha.esperar(5_000);
            assertEquals(1, senales.length, "el commit debe despertar al listener");
            assertEquals(SenalTrabajoPush.CANAL, senales[0].getName());
            assertEquals("", senales[0].getParameter());
        }
    }

    @Test
    void elBorradorNoGeneraNadaYPublicarloGeneraAvisoYTrabajo() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());

        SolicitudResult borrador = guardarBorrador.ejecutar(new GuardarBorradorCommand(
                datos.cliente(), datos.categoria(), "Descripción", datos.zona(), "MEDIA"));
        assertEquals(0, contarAvisos(datos.prestador()));
        assertEquals(0, contarTrabajos(datos.prestador()));

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            publicar.ejecutar(new PublicarSolicitudCommand(borrador.id(), datos.cliente()));

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
            assertEquals(1, escucha.esperar(5_000).length);
        }
    }

    @Test
    void laSenalNoLlegaAntesDelCommitYLlegaAlConfirmar() throws Exception {
        Datos datos = sembrar();
        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                registrarAviso.ejecutar(new RegistrarAvisoOportunidadCommand(UUID.randomUUID(),
                        datos.prestador(), 1, "detalle"));
                try {
                    assertEquals(0, escucha.esperar(600).length, "NOTIFY no se entrega antes del commit");
                } catch (SQLException ex) {
                    throw new IllegalStateException(ex);
                }
            });

            assertEquals(1, escucha.esperar(5_000).length, "el commit entrega la señal");
            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
        }
    }

    @Test
    void reprocesarElEventoNoDuplicaFilasNiEmiteOtraSenal() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());
        SolicitudResult publicada = crear.ejecutar(new CrearSolicitudCommand(datos.cliente(), datos.categoria(),
                "Descripción", datos.zona(), "MEDIA", "PUBLICADA"));

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> publicador.publicar(
                    new Eventos.SolicitudPublicada(publicada.id(), datos.cliente(), datos.categoria(), datos.zona(),
                            "MEDIA", OffsetDateTime.now())));

            assertEquals(1, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
            assertEquals(0, escucha.esperar(1_500).length);
        }
    }

    @Test
    void elRollbackRevierteSolicitudAvisoYTrabajoSinEmitirSenal() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());
        registro.fallar = true;

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            assertThrows(IllegalStateException.class, () -> crear.ejecutar(new CrearSolicitudCommand(
                    datos.cliente(), datos.categoria(), "Descripción", datos.zona(), "MEDIA", "PUBLICADA")));

            assertEquals(0, contarSolicitudes(datos.cliente()));
            assertEquals(0, contarAvisos(datos.prestador()));
            assertEquals(0, contarTrabajos(datos.prestador()));
            assertEquals(0, escucha.esperar(1_500).length);
        }
    }

    @Test
    void dosConsumosConcurrentesDelMismoEventoNoAbortanNiDuplicanFilas() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());
        UUID solicitud = UUID.randomUUID();
        CyclicBarrier barrera = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> consumos = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                consumos.add(pool.submit(() -> {
                    barrera.await(5, TimeUnit.SECONDS);
                    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                        UUID marcador = UUID.randomUUID();
                        jdbc.update("""
                                INSERT INTO solicitudes (id, cliente_id, categoria_id, zona_id, descripcion,
                                                         urgencia, estado, fecha_creacion, fecha_actualizacion, version)
                                VALUES (?, ?, ?, ?, ?, 'MEDIA', 'PUBLICADA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                                """, marcador, datos.cliente(), datos.categoria(), datos.zona(), "marca");
                        registrarAviso.ejecutar(new RegistrarAvisoOportunidadCommand(solicitud,
                                datos.prestador(), 1, "detalle"));
                        // Si la unicidad abortara la transacción, esta escritura fallaría.
                        jdbc.update("UPDATE solicitudes SET descripcion = ? WHERE id = ?", "actualizada", marcador);
                    });
                    return null;
                }));
            }
            for (Future<?> consumo : consumos) {
                consumo.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(2, contarSolicitudes(datos.cliente()));
        assertEquals(1, contarAvisos(datos.prestador()));
        assertEquals(1, contarTrabajos(datos.prestador()));
    }

    @Test
    void preferenciaApagadaConservaElAvisoYNoProgramaPushRetroactivo() throws Exception {
        Datos datos = sembrar();
        compatibilidad.prestadores = List.of(datos.prestador());
        preferencias.habilitado = false;

        try (EscuchaSenalTrabajoPush escucha = EscuchaSenalTrabajoPush.abrir()) {
            SolicitudResult publicada = crear.ejecutar(new CrearSolicitudCommand(datos.cliente(), datos.categoria(),
                    "Descripción", datos.zona(), "MEDIA", "PUBLICADA"));

            assertEquals(1, contarAvisos(datos.prestador()), "la bandeja conserva el aviso con push apagado");
            assertEquals(0, contarTrabajos(datos.prestador()));
            assertEquals(0, escucha.esperar(1_500).length);

            preferencias.habilitado = true;
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> publicador.publicar(
                    new Eventos.SolicitudPublicada(publicada.id(), datos.cliente(), datos.categoria(), datos.zona(),
                            "MEDIA", OffsetDateTime.now())));
            assertEquals(0, contarTrabajos(datos.prestador()), "reactivar no encola avisos históricos");

            crear.ejecutar(new CrearSolicitudCommand(datos.cliente(), datos.categoria(),
                    "Otra", datos.zona(), "MEDIA", "PUBLICADA"));
            assertEquals(2, contarAvisos(datos.prestador()));
            assertEquals(1, contarTrabajos(datos.prestador()));
            assertEquals(1, escucha.esperar(5_000).length);
        }
    }

    private Datos sembrar() {
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
        return new Datos(cliente, prestador, categoria, zona);
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

    private int contarSolicitudes(UUID cliente) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM solicitudes WHERE cliente_id = ?",
                Integer.class, cliente);
        return total == null ? 0 : total;
    }

    private static String leer(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }

    private record Datos(UUID cliente, UUID prestador, UUID categoria, UUID zona) {}

    @TestConfiguration
    static class Sustituciones {
        @Bean @Primary CompatibilidadFalsa compatibilidadFalsa() { return new CompatibilidadFalsa(); }

        @Bean @Primary PreferenciasFalsas preferenciasFalsas() { return new PreferenciasFalsas(); }

        @Bean @Primary CanalBloqueante canalBloqueante() { return new CanalBloqueante(); }

        @Bean @Primary RegistroConFallo registroConFallo(RegistroOportunidadNotificacionAdapter real) {
            return new RegistroConFallo(real);
        }
    }

    static final class CompatibilidadFalsa implements CompatibilidadPort {
        volatile List<UUID> prestadores = List.of();

        @Override public boolean esCompatible(UUID solicitudId, UUID prestadorId) {
            return prestadores.contains(prestadorId);
        }

        @Override public List<UUID> prestadoresCompatibles(UUID solicitudId) { return prestadores; }
    }

    static final class PreferenciasFalsas implements ConsultaPreferenciasPort {
        volatile boolean habilitado = true;

        @Override public boolean avisoHabilitado(UUID usuarioId, String tipoAviso, String canal) {
            return habilitado;
        }
    }

    /** Si alguien lo invocara durante la publicación, bloquearía; el caso de uso no lo llama. */
    static final class CanalBloqueante implements CanalEntregaNotificacion {
        final AtomicInteger entregas = new AtomicInteger();

        @Override public boolean disponibleParaEntrega(Notificacion notificacion) { return false; }

        @Override public boolean entregar(Notificacion notificacion) {
            entregas.incrementAndGet();
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /** Delega en el registro real y, si se activa, falla después de encolar. */
    static final class RegistroConFallo implements RegistroNotificacionPort {
        private final RegistroNotificacionPort real;
        volatile boolean fallar;

        RegistroConFallo(RegistroNotificacionPort real) {
            this.real = real;
        }

        @Override public void registrar(UUID solicitudId, UUID prestadorId, int version, String detalle) {
            real.registrar(solicitudId, prestadorId, version, detalle);
            if (fallar) {
                throw new IllegalStateException("Fallo inyectado después de encolar");
            }
        }
    }
}
