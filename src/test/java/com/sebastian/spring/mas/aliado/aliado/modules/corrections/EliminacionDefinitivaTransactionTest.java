package com.sebastian.spring.mas.aliado.aliado.modules.corrections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.EliminacionDefinitivaPort;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class EliminacionDefinitivaTransactionTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired EliminacionDefinitivaPort eliminacion;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void falloParcialRevierteAnonimizacionYConservaFilas() {
        UUID usuarioId = UUID.randomUUID();
        String email = "rollback-" + usuarioId + "@example.test";
        OffsetDateTime ahora = OffsetDateTime.now();
        jdbc.update("insert into usuarios (id,email,password_hash,rol,estado_cuenta,fecha_creacion) values (?,?,?,?,?,?)",
                usuarioId, email, "hash", "CLIENTE", "ACTIVA", ahora);
        jdbc.update("insert into perfiles (usuario_id,nombre,telefono,preferencia_visibilidad,version) values (?,?,?,?,0)",
                usuarioId, "Persona temporal", "3000000000", "SOLO_ZONA");
        UUID solicitudId = UUID.randomUUID();
        UUID operacionId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();
        jdbc.update("insert into solicitudes_eliminacion (id,usuario_id,fecha_solicitud,fecha_expiracion,estado) "
                        + "values (?,?,?,?,?)", operacionId, usuarioId, ahora, ahora.plusHours(24), "PENDIENTE");
        jdbc.update("insert into solicitudes (id,cliente_id,categoria_id,zona_id,descripcion,urgencia,estado,"
                        + "fecha_creacion,fecha_actualizacion,version) values (?,?,?,?,?,?,?,?,?,0)",
                solicitudId, usuarioId, UUID.fromString("11111111-1111-1111-1111-111111111103"),
                UUID.fromString("22222222-2222-2222-2222-222222222201"), "Solicitud rollback", "MEDIA",
                "PUBLICADA", ahora, ahora);
        jdbc.update("insert into fotos_solicitud (id,solicitud_id,archivo_key,orden) values (?,?,?,1)",
                fotoId, solicitudId, "rollback-file.png");

        var tx = new TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
            eliminacion.anonimizarYEncolarArchivos(usuarioId, operacionId);
            throw new IllegalStateException("fallo simulado");
        }));

        assertEquals("ACTIVA", jdbc.queryForObject("select estado_cuenta from usuarios where id = ?", String.class, usuarioId));
        assertEquals(email, jdbc.queryForObject("select email from usuarios where id = ?", String.class, usuarioId));
        assertNotNull(jdbc.queryForObject("select count(*) from perfiles where usuario_id = ?", Integer.class, usuarioId));
        assertEquals(1, jdbc.queryForObject("select count(*) from perfiles where usuario_id = ?", Integer.class, usuarioId));
        assertEquals(1, jdbc.queryForObject("select count(*) from fotos_solicitud where id = ?", Integer.class, fotoId));
        assertEquals(0, jdbc.queryForObject("select count(*) from archivos_pendientes_eliminacion where clave = ?",
                Integer.class, "rollback-file.png"));
    }


    @Test
    void eliminaAvisosConTrabajosPushYRevocaElTokenDeLaCuenta() {
        UUID usuario = UUID.randomUUID();
        UUID instalacion = UUID.randomUUID();
        UUID aviso = UUID.randomUUID();
        UUID trabajo = UUID.randomUUID();
        UUID entrega = UUID.randomUUID();
        jdbc.update("""
                insert into usuarios (id,email,password_hash,rol,estado_cuenta,fecha_creacion)
                values (?,?,'hash','PRESTADOR','ACTIVA',current_timestamp)
                """, usuario, "push-" + usuario + "@example.test");
        jdbc.update("""
                insert into instalaciones_push (id,plataforma,prueba_sha256)
                values (?,'ANDROID',sha256(convert_to('prueba','UTF8')))
                """, instalacion);
        jdbc.update("""
                insert into vinculos_push (instalacion_id,generacion,usuario_id,token_fcm,
                    token_fcm_sha256,revocacion_sha256)
                values (?,1,?,'token-prueba',sha256(convert_to('token-prueba','UTF8')),
                    sha256(convert_to('revocacion','UTF8')))
                """, instalacion, usuario);
        jdbc.update("""
                insert into notificaciones (id,destinatario_id,tipo,aggregate_id,event_version,
                    detalle,canal,estado_entrega,intentos,fecha_generacion)
                values (?,?,'NUEVA_OPORTUNIDAD',?,1,'Aviso','PUSH','PENDIENTE',0,current_timestamp)
                """, aviso, usuario, UUID.randomUUID());
        jdbc.update("""
                insert into trabajos_push (id,notificacion_id,canal,estado,expira_en)
                values (?,?,'PUSH','PREPARADO',current_timestamp + interval '1 hour')
                """, trabajo, aviso);
        jdbc.update("""
                insert into entregas_push (id,trabajo_id,instalacion_id,generacion,estado,proximo_intento_en)
                values (?,?,?,1,'PENDIENTE',current_timestamp)
                """, entrega, trabajo, instalacion);

        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                eliminacion.anonimizarYEncolarArchivos(usuario, UUID.randomUUID()));

        assertEquals("ELIMINADA", jdbc.queryForObject(
                "select estado_cuenta from usuarios where id = ?", String.class, usuario));
        assertEquals(0, jdbc.queryForObject("select count(*) from notificaciones where id = ?", Integer.class, aviso));
        assertEquals(0, jdbc.queryForObject("select count(*) from trabajos_push where id = ?", Integer.class, trabajo));
        assertEquals(0, jdbc.queryForObject("select count(*) from entregas_push where id = ?", Integer.class, entrega));
        assertEquals(1, jdbc.queryForObject("""
                select count(*) from vinculos_push where instalacion_id = ?
                and revocado_en is not null and token_fcm is null and token_fcm_sha256 is null
                """, Integer.class, instalacion));
    }
}
