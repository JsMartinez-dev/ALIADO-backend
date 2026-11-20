package com.sebastian.spring.mas.aliado.aliado.modules.corrections;

import static org.junit.jupiter.api.Assertions.*;

import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotencyRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.EstadoCuenta;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Rol;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers(disabledWithoutDocker = true)
class PostgresJsonbIntegrationTest {
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

    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilProfesionalRepository perfiles;
    @Autowired IdempotencyRepository idempotencia;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    @Test
    void perfilYPistaDeIdempotenciaPersistenComoJsonb() {
        var ahora = OffsetDateTime.now();
        var usuario = usuarios.guardar(new Usuario(UUID.randomUUID(), "jsonb-" + UUID.randomUUID() + "@test.com",
                "hash", Rol.PRESTADOR, EstadoCuenta.ACTIVA, ahora));
        var perfil = new PerfilProfesional(UUID.randomUUID(), usuario.id(),
                Map.of("descripcion", "Plomería", "especialidades", List.of("emergencias")),
                EstadoExposicion.OCULTO, ahora, ahora, Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of());

        perfiles.guardar(perfil);
        entityManager.flush();

        assertEquals("jsonb", jdbc.queryForObject(
                "select pg_typeof(datos_generales)::text from perfiles_profesionales where id = ?",
                String.class, perfil.id()));
        assertEquals("Plomería", perfiles.porPrestadorId(usuario.id()).orElseThrow()
                .datosGenerales().get("descripcion"));

        UUID clave = UUID.randomUUID();
        idempotencia.guardar(clave, "test.jsonb", usuario.id(), 200, "{\"ok\":true}");
        entityManager.flush();

        assertEquals("jsonb", jdbc.queryForObject(
                "select pg_typeof(respuesta)::text from idempotency_keys where clave = ? and operacion = ?",
                String.class, clave, "test.jsonb"));
        assertEquals("{\"ok\":true}", idempotencia.buscar(clave, "test.jsonb").orElseThrow().cuerpoJson());
    }
}
