package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.EliminacionPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ServicioBloqueaEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.SinSolicitudPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.ConsultaServiciosActivosPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.EliminacionDefinitivaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.EstadoEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.model.SolicitudEliminacion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.domain.repository.SolicitudEliminacionRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EliminacionCuentaServiceTest {
    private final UUID usuario = UUID.randomUUID();

    @Test
    void servicioActivoBloqueaSolicitudYConfirmacion() {
        var repo = new Repo();
        var bloqueo = new ServiciosActivos(true);
        var solicitar = new SolicitarEliminacionCuentaService(repo, bloqueo);

        assertThrows(ServicioBloqueaEliminacionException.class, () -> solicitar.ejecutar(usuario));
        assertTrue(repo.datos.isEmpty());

        repo.datos.put(usuario, pendiente(OffsetDateTime.now().plusHours(1)));
        assertThrows(ServicioBloqueaEliminacionException.class,
                () -> new ConfirmarEliminacionCuentaService(repo, bloqueo, new Definitiva(), evento -> { }).ejecutar(usuario));
        assertEquals(EstadoEliminacion.PENDIENTE, repo.ultimaDeUsuario(usuario).orElseThrow().estado());
    }

    @Test
    void solicitudRegistraEstadoPendienteYDuplicadoEsRechazado() {
        var repo = new Repo();
        var solicitar = new SolicitarEliminacionCuentaService(repo, new ServiciosActivos(false));

        var result = solicitar.ejecutar(usuario);

        assertEquals("PENDIENTE", result.estado());
        assertTrue(repo.ultimaDeUsuario(usuario).orElseThrow().fechaExpiracion()
                .isAfter(OffsetDateTime.now().plusHours(23)));
        assertThrows(EliminacionPendienteException.class, () -> solicitar.ejecutar(usuario));
    }

    @Test
    void confirmacionMarcaEstadoYPublicaEvento() {
        var repo = new Repo();
        repo.datos.put(usuario, pendiente(OffsetDateTime.now().plusHours(1)));
        var eventos = new ArrayList<Object>();

        var definitiva = new Definitiva();
        var result = new ConfirmarEliminacionCuentaService(repo, new ServiciosActivos(false), definitiva, eventos::add)
                .ejecutar(usuario);

        assertEquals("CONFIRMADA", result.estado());
        assertEquals(repo.ultimaDeUsuario(usuario).orElseThrow().id(), result.operacionId());
        assertEquals(result.operacionId(), definitiva.operacionId);
        assertEquals(EstadoEliminacion.CONFIRMADA, repo.ultimaDeUsuario(usuario).orElseThrow().estado());
        assertEquals(1, eventos.size());
        assertTrue(eventos.getFirst() instanceof Eventos.EliminacionConfirmada);
    }

    @Test
    void solicitudVencidaNoConfirmaNiBloquea() {
        var repo = new Repo();
        repo.datos.put(usuario, pendiente(OffsetDateTime.now().minusMinutes(1)));
        var eventos = new ArrayList<Object>();

        var result = new ConfirmarEliminacionCuentaService(repo, new ServiciosActivos(false), new Definitiva(), eventos::add)
                .ejecutar(usuario);

        assertEquals("EXPIRADA", result.estado());
        assertEquals(EstadoEliminacion.EXPIRADA, repo.ultimaDeUsuario(usuario).orElseThrow().estado());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void confirmacionRepetidaEsIdempotente() {
        var repo = new Repo();
        repo.datos.put(usuario, pendiente(OffsetDateTime.now().plusHours(1)));
        var definitiva = new Definitiva();
        var events = new ArrayList<Object>();
        var service = new ConfirmarEliminacionCuentaService(repo, new ServiciosActivos(false), definitiva, events::add);

        service.ejecutar(usuario);
        service.ejecutar(usuario);

        assertEquals(1, definitiva.ejecuciones);
        assertEquals(1, events.size());
        assertEquals(EstadoEliminacion.CONFIRMADA, repo.ultimaDeUsuario(usuario).orElseThrow().estado());
    }

    @Test
    void confirmarSinSolicitudFalla() {
        var repo = new Repo();
        assertThrows(SinSolicitudPendienteException.class,
                () -> new ConfirmarEliminacionCuentaService(repo, new ServiciosActivos(false), new Definitiva(), evento -> { })
                        .ejecutar(usuario));
    }

    @Test
    void expiracionMasivaSoloAfectaPendientesVencidas() {
        var repo = new Repo();
        repo.datos.put(usuario, pendiente(OffsetDateTime.now().minusMinutes(1)));
        new ExpirarSolicitudesEliminacionService(repo).ejecutar();
        assertEquals(EstadoEliminacion.EXPIRADA, repo.ultimaDeUsuario(usuario).orElseThrow().estado());
        assertFalse(repo.ultimaDeUsuario(usuario).orElseThrow().vencida(OffsetDateTime.now()));
    }

    private SolicitudEliminacion pendiente(OffsetDateTime expira) {
        return new SolicitudEliminacion(UUID.randomUUID(), usuario, OffsetDateTime.now().minusMinutes(5),
                expira, EstadoEliminacion.PENDIENTE, null);
    }

    private static final class ServiciosActivos implements ConsultaServiciosActivosPort {
        private final boolean bloquea;

        private ServiciosActivos(boolean bloquea) {
            this.bloquea = bloquea;
        }

        @Override
        public boolean bloqueaEliminacion(UUID usuarioId) {
            return bloquea;
        }
    }

    private static final class Definitiva implements EliminacionDefinitivaPort {
        private boolean eliminada;
        private int ejecuciones;

        @Override
        public boolean estaEliminada(UUID usuarioId) {
            return eliminada;
        }

        private UUID operacionId;

        @Override
        public void anonimizarYEncolarArchivos(UUID usuarioId, UUID operacionId) {
            eliminada = true;
            ejecuciones++;
            this.operacionId = operacionId;
        }
    }

    private static final class Repo implements SolicitudEliminacionRepository {
        final Map<UUID, SolicitudEliminacion> datos = new HashMap<>();

        @Override
        public Optional<SolicitudEliminacion> pendienteDeUsuario(UUID usuarioId) {
            return ultimaDeUsuario(usuarioId).filter(s -> s.estado() == EstadoEliminacion.PENDIENTE);
        }

        @Override
        public List<SolicitudEliminacion> pendientesVencidas(OffsetDateTime ahora) {
            return datos.values().stream().filter(s -> s.vencida(ahora)).toList();
        }

        @Override
        public SolicitudEliminacion guardar(SolicitudEliminacion solicitud) {
            datos.put(solicitud.usuarioId(), solicitud);
            return solicitud;
        }

        @Override
        public Optional<SolicitudEliminacion> ultimaDeUsuario(UUID usuarioId) {
            return Optional.ofNullable(datos.get(usuarioId));
        }

        @Override
        public boolean transicionar(UUID id, EstadoEliminacion esperado, EstadoEliminacion nuevo,
                                    OffsetDateTime fechaConfirmacion) {
            var actual = datos.values().stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
            if (actual == null || actual.estado() != esperado) {
                return false;
            }
            datos.put(actual.usuarioId(), new SolicitudEliminacion(actual.id(), actual.usuarioId(),
                    actual.fechaSolicitud(), actual.fechaExpiracion(), nuevo, fechaConfirmacion));
            return true;
        }

        @Override
        public void expirarVencidas(OffsetDateTime ahora) {
            datos.replaceAll((id, s) -> s.vencida(ahora)
                    ? new SolicitudEliminacion(s.id(), s.usuarioId(), s.fechaSolicitud(),
                            s.fechaExpiracion(), EstadoEliminacion.EXPIRADA, null)
                    : s);
        }
    }
}
